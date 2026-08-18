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
			document.execCommand('fontSize', false, e.target.value);
		});
	}

	const fontColorInput = document.getElementById('dcFontColor');
	if (fontColorInput) {
		fontColorInput.addEventListener('input', (e) => {
			if (!restoreRegionSelection()) return;
			document.execCommand('foreColor', false, e.target.value);
		});
	}

	function handleImagePaste(e, targetRegion) {
		const items = (e.clipboardData || window.clipboardData).items;
		if (!items) return;
		let handled = false;

		for (const item of items) {
			if (item.type.indexOf('image') === 0) {
				handled = true;
				const blob = item.getAsFile();
				const reader = new FileReader();
				reader.onload = function (ev) {
					const dataUrl = ev.target.result;
					const img = document.createElement('img');
					img.src = dataUrl;

					if (targetRegion.classList.contains('dc-region-image')) {
						targetRegion.innerHTML = '';
						targetRegion.appendChild(img);
						targetRegion.dispatchEvent(new CustomEvent('dc:image-set'));
					} else {
						img.style.maxWidth = '100%';
						const sel = window.getSelection();
						if (sel.rangeCount) {
							const range = sel.getRangeAt(0);
							range.deleteContents();
							range.insertNode(img);
							range.collapse(false);
						} else {
							targetRegion.appendChild(img);
						}
					}
				};
				reader.readAsDataURL(blob);
			}
		}

		if (handled) e.preventDefault();
	}

	document.querySelectorAll('.dc-region-image, .dc-region[data-dc-region="text"]').forEach(region => {
		region.addEventListener('paste', (e) => handleImagePaste(e, region));
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
		});

		region.addEventListener('keydown', (e) => {
			if ((e.key === 'Backspace' || e.key === 'Delete') && region.querySelector('img')) {
				e.preventDefault();
				resetToPlaceholder();
			}
		});
	});

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
