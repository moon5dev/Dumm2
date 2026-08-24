(function () {
	let savedRange = null;
	let savedRegion = null;

	document.addEventListener('selectionchange', () => {
		const sel = window.getSelection();
		if (!sel.rangeCount) return;
		const range = sel.getRangeAt(0);
		const node = range.commonAncestorContainer;
		const el = node.nodeType === 3 ? node.parentElement : node;
		const region = el ? el.closest('.dc-region') : null;
		if (region) {
			savedRange = range.cloneRange();
			savedRegion = region;
		}
	});

	function restoreRegionSelection() {
		if (!savedRange || !savedRegion) return false;
		savedRegion.focus();
		const sel = window.getSelection();
		sel.removeAllRanges();
		sel.addRange(savedRange);
		return true;
	}

	document.querySelectorAll('.dc-region[data-dc-region="text"]').forEach(region => {
		region.addEventListener('keydown', (e) => {
			const sel = window.getSelection();
			if (!sel.rangeCount) return;
			const range = sel.getRangeAt(0);
			const atStart = range.collapsed && range.startOffset === 0 &&
				(range.startContainer === region || range.startContainer === region.firstChild);
			const isEmpty = region.textContent.trim().length === 0;

			if (e.key === 'Backspace' && (atStart || isEmpty)) {
				e.preventDefault();
			}
		});
	});

	document.querySelectorAll('.dc-toolbar button[data-cmd]').forEach(btn => {
		btn.addEventListener('mousedown', (e) => e.preventDefault());
		btn.addEventListener('click', () => {
			const active = document.activeElement;
			if (!active || !active.classList.contains('dc-region')) return;
			document.execCommand(btn.dataset.cmd, false, null);
		});
	});

	const fontSizeSel = document.getElementById('dcFontSize');
	if (fontSizeSel) {
		fontSizeSel.addEventListener('change', (e) => {
			if (!restoreRegionSelection()) return;
			const px = e.target.value;
			// execCommand only understands legacy sizes 1-7, so use a placeholder
			// size then swap the resulting <font size="7"> tags for real px spans.
			document.execCommand('fontSize', false, '7');
			savedRegion.querySelectorAll('font[size="7"]').forEach((f) => {
				const span = document.createElement('span');
				span.style.fontSize = px + 'px';
				while (f.firstChild) span.appendChild(f.firstChild);
				f.parentNode.replaceChild(span, f);
			});
		});
	}

	const fontColorInput = document.getElementById('dcFontColor');
	if (fontColorInput) {
		fontColorInput.addEventListener('input', (e) => {
			if (!restoreRegionSelection()) return;
			document.execCommand('foreColor', false, e.target.value);
		});
	}

	function makeImageResizable(img) {
		if (img.closest('.dc-img-resize-wrap')) {
			return img.closest('.dc-img-resize-wrap');
		}

		const handle = document.createElement('span');
		handle.className = 'dc-img-resize-handle dc-no-print';
		handle.setAttribute('aria-label', 'Resize image');

		handle.addEventListener('mousedown', (e) => {
			e.preventDefault();
			e.stopPropagation();

			const startX = e.clientX;
			const startY = e.clientY;
			const startWidth = img.offsetWidth;
			const startHeight = img.offsetHeight;
			const ratio = img.naturalWidth && img.naturalHeight ? img.naturalWidth / img.naturalHeight : (img.offsetWidth / img.offsetHeight || 1);

			function onMove(ev) {
				const newWidth = Math.max(30, startWidth + (ev.clientX - startX));
				const newHeight = Math.max(30, startHeight + (ev.clientY - startY));
				img.style.maxWidth = 'none';
				img.style.maxHeight = 'none';
				img.style.width = newWidth + 'px';
				img.style.height = ev.shiftKey ? (newWidth / ratio) + 'px' : newHeight + 'px';
			}
			function onUp() {
				document.removeEventListener('mousemove', onMove);
				document.removeEventListener('mouseup', onUp);
			}
			document.addEventListener('mousemove', onMove);
			document.addEventListener('mouseup', onUp);
		});

		const wrap = document.createElement('span');
		wrap.className = 'dc-img-resize-wrap';
		wrap.appendChild(img);
		wrap.appendChild(handle);
		return wrap;
	}

	function wrapExistingImages() {
		document.querySelectorAll('.doc img').forEach((img) => {
			if (img.closest('.dc-img-resize-wrap')) return;
			const marker = document.createTextNode('');
			img.parentNode.insertBefore(marker, img);
			const wrap = makeImageResizable(img);
			marker.parentNode.replaceChild(wrap, marker);
		});
	}

	function insertImageFile(file, targetRegion) {
		if (!file || file.type.indexOf('image') !== 0) return false;

		const reader = new FileReader();
		reader.onload = function (ev) {
			const dataUrl = ev.target.result;
			const img = document.createElement('img');
			img.src = dataUrl;
			const wrap = makeImageResizable(img);

			if (targetRegion.classList.contains('dc-region-image')) {
				targetRegion.innerHTML = '';
				targetRegion.appendChild(wrap);
				targetRegion.dispatchEvent(new CustomEvent('dc:image-set'));
			} else {
				img.style.maxWidth = '100%';
				const sel = window.getSelection();
				if (sel.rangeCount) {
					const range = sel.getRangeAt(0);
					range.deleteContents();
					range.insertNode(wrap);
					range.collapse(false);
				} else {
					targetRegion.appendChild(wrap);
				}
			}
		};
		reader.readAsDataURL(file);
		return true;
	}

	function handleImagePaste(e, targetRegion) {
		const items = (e.clipboardData || window.clipboardData).items;
		if (!items) return;
		let handled = false;

		for (const item of items) {
			if (item.type.indexOf('image') === 0) {
				handled = insertImageFile(item.getAsFile(), targetRegion) || handled;
			}
		}

		if (handled) e.preventDefault();
	}

	function handleImageDrop(e, targetRegion) {
		const files = e.dataTransfer && e.dataTransfer.files;
		if (!files || !files.length) return;
		let handled = false;

		for (const file of files) {
			handled = insertImageFile(file, targetRegion) || handled;
		}

		if (handled) e.preventDefault();
		targetRegion.classList.remove('dc-drag-over');
	}

	document.querySelectorAll('.dc-region-image, .dc-region[data-dc-region="text"]').forEach(region => {
		region.addEventListener('paste', (e) => handleImagePaste(e, region));
		region.addEventListener('dragover', (e) => {
			e.preventDefault();
			region.classList.add('dc-drag-over');
		});
		region.addEventListener('dragleave', () => region.classList.remove('dc-drag-over'));
		region.addEventListener('drop', (e) => handleImageDrop(e, region));
	});

	document.querySelectorAll('.dc-region-image').forEach(region => {
		const placeholderHTML = region.innerHTML;

		const removeBtn = document.createElement('button');
		removeBtn.type = 'button';
		removeBtn.className = 'dc-region-image-remove dc-no-print';
		removeBtn.textContent = '×';
		removeBtn.setAttribute('aria-label', 'Remove image');
		removeBtn.hidden = true;
		region.appendChild(removeBtn);

		function resetToPlaceholder() {
			region.innerHTML = placeholderHTML;
			region.appendChild(removeBtn);
			removeBtn.hidden = true;
			region.classList.remove('dc-has-image');
		}

		removeBtn.addEventListener('click', (e) => {
			e.stopPropagation();
			resetToPlaceholder();
			region.focus();
		});

		region.addEventListener('click', () => region.focus());

		region.addEventListener('dc:image-set', () => {
			region.appendChild(removeBtn);
			removeBtn.hidden = false;
			region.classList.add('dc-has-image');
		});

		region.addEventListener('keydown', (e) => {
			if ((e.key === 'Backspace' || e.key === 'Delete') && region.querySelector('img')) {
				e.preventDefault();
				resetToPlaceholder();
			}
		});

		if (region.querySelector('img')) {
			removeBtn.hidden = false;
			region.classList.add('dc-has-image');
		}
	});

	wrapExistingImages();

	const printBtn = document.getElementById('dcPrintBtn');
	if (printBtn) {
		printBtn.addEventListener('click', () => window.print());
	}

	window.addEventListener('beforeprint', () => {
		document.querySelectorAll('.dc-region-select').forEach(select => {
			let printSpan = select.nextElementSibling;
			if (!printSpan || !printSpan.classList.contains('dc-region-select-print')) {
				printSpan = document.createElement('span');
				printSpan.className = 'dc-region-select-print';
				select.after(printSpan);
			}
			const selected = select.options[select.selectedIndex];
			printSpan.textContent = (selected && selected.value !== '') ? selected.text : '';
		});
	});
})();
