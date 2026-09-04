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

		function getContentWidth(el) {
			if (!el) return 0;
			const style = window.getComputedStyle(el);
			const paddingX = parseFloat(style.paddingLeft || 0) + parseFloat(style.paddingRight || 0);
			return Math.max(0, el.clientWidth - paddingX);
		}

		function getResizeBounds() {
			const container = img.closest('.dc-region-image, td, th, .dc-region, .doc');
			const maxWidth = getContentWidth(container) || img.offsetWidth || 30;
			return { maxWidth: Math.max(30, maxWidth) };
		}

		function syncImageBox(wrap, preferNaturalSize) {
			const bounds = getResizeBounds();
			const renderedWidth = preferNaturalSize ? 0 : img.offsetWidth;
			const renderedHeight = preferNaturalSize ? 0 : img.offsetHeight;
			const currentWidth = renderedWidth || img.naturalWidth || 120;
			const currentHeight = renderedHeight || img.naturalHeight || 80;
			const width = Math.min(Math.max(30, currentWidth), bounds.maxWidth);
			const height = Math.max(30, currentHeight);
			wrap.style.width = width + 'px';
			wrap.style.height = height + 'px';
			img.style.width = '100%';
			img.style.height = '100%';
			img.style.maxWidth = 'none';
			img.style.maxHeight = 'none';
		}

		const handle = document.createElement('span');
		handle.className = 'dc-img-resize-handle dc-no-print';
		handle.setAttribute('aria-label', 'Resize image');

		handle.addEventListener('mousedown', (e) => {
			e.preventDefault();
			e.stopPropagation();

			const startX = e.clientX;
			const startY = e.clientY;
			const wrap = img.closest('.dc-img-resize-wrap');
			const startWidth = wrap.offsetWidth;
			const startHeight = wrap.offsetHeight;
			const ratio = img.naturalWidth && img.naturalHeight ? img.naturalWidth / img.naturalHeight : (img.offsetWidth / img.offsetHeight || 1);

			function onMove(ev) {
				const bounds = getResizeBounds();
				const newWidth = Math.min(bounds.maxWidth, Math.max(30, startWidth + (ev.clientX - startX)));
				const newHeight = Math.max(30, startHeight + (ev.clientY - startY));
				wrap.style.width = newWidth + 'px';
				wrap.style.height = ev.shiftKey ? (newWidth / ratio) + 'px' : newHeight + 'px';
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
		if (img.complete && img.naturalWidth) {
			window.requestAnimationFrame(() => syncImageBox(wrap, false));
		} else {
			img.addEventListener('load', () => syncImageBox(wrap, true), { once: true });
		}
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

	function getImagePlaceholderHTML(region) {
		const placeholder = region.querySelector('.dc-region-placeholder');
		if (placeholder) return placeholder.outerHTML;
		return '<span class="dc-region-placeholder">Click, then paste an image with Ctrl+V</span>';
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
		const placeholderHTML = getImagePlaceholderHTML(region);

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

	// Strips UI-only DOM that workspace.js itself injects at load time (image
	// remove buttons, resize handles, the print-only <select> mirror) before
	// a draft is saved. Without this, saved HTML round-trips back through the
	// same injection code on next load — which has no "already has one" guard
	// for these — so a stray extra copy gets appended every save/reload cycle.
	function getCleanDraftHtml(doc) {
		const clone = doc.cloneNode(true);
		const sourceControls = doc.querySelectorAll('input, select, textarea');
		const cloneControls = clone.querySelectorAll('input, select, textarea');

		sourceControls.forEach((sourceControl, index) => {
			const cloneControl = cloneControls[index];
			if (!cloneControl) return;

			if (sourceControl.matches('input[type="checkbox"], input[type="radio"]')) {
				if (sourceControl.checked) {
					cloneControl.setAttribute('checked', 'checked');
				} else {
					cloneControl.removeAttribute('checked');
				}
				return;
			}

			if (sourceControl.tagName === 'SELECT') {
				[...sourceControl.options].forEach((option, optionIndex) => {
					const cloneOption = cloneControl.options[optionIndex];
					if (!cloneOption) return;
					if (option.selected) {
						cloneOption.setAttribute('selected', 'selected');
					} else {
						cloneOption.removeAttribute('selected');
					}
				});
				return;
			}

			if (sourceControl.tagName === 'TEXTAREA') {
				cloneControl.textContent = sourceControl.value;
				return;
			}

			cloneControl.setAttribute('value', sourceControl.value);
		});
		clone.querySelectorAll('.dc-no-print, .dc-region-select-print').forEach((el) => el.remove());
		return clone.innerHTML;
	}

	const saveDraftBtn = document.getElementById('dcSaveDraftBtn');
	if (saveDraftBtn) {
		saveDraftBtn.addEventListener('click', async () => {
			const docs = [...document.querySelectorAll('.doc[data-template-id]')];
			saveDraftBtn.disabled = true;
			const originalText = saveDraftBtn.textContent;
			saveDraftBtn.textContent = 'Saving...';
			try {
				await Promise.all(docs.map((doc) => {
					const body = new URLSearchParams();
					body.set('templateId', doc.dataset.templateId);
					body.set('contentHtml', getCleanDraftHtml(doc));
					return fetch('/workspace/draft', { method: 'POST', body }).then((response) => {
						if (!response.ok) throw new Error('Failed to save draft.');
					});
				}));
				saveDraftBtn.textContent = 'Saved';
			} catch (e) {
				saveDraftBtn.textContent = 'Save failed';
			} finally {
				setTimeout(() => {
					saveDraftBtn.textContent = originalText;
					saveDraftBtn.disabled = false;
				}, 1500);
			}
		});
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
