(function () {
	const textarea = document.getElementById('dcContentEditor');
	if (!textarea || typeof Jodit === 'undefined') return;

	function findSelectionRegion(editor) {
		const sel = editor.editorWindow.getSelection();
		if (!sel || !sel.rangeCount) return null;
		const node = sel.getRangeAt(0).commonAncestorContainer;
		const el = node.nodeType === 3 ? node.parentElement : node;
		return el ? el.closest('[data-dc-region]') : null;
	}

	function appendAfterRegionContent(region, node) {
		const container = region.closest('td, th') || region.parentElement;
		if (container) {
			container.appendChild(node);
		} else {
			region.after(node);
		}
	}

	function placeCaretAtEnd(editor, node) {
		const range = editor.editorDocument.createRange();
		const selection = editor.editorWindow.getSelection();
		range.selectNodeContents(node);
		range.collapse(false);
		selection.removeAllRanges();
		selection.addRange(range);
	}

	function appendPlainTextLineAfterRegion(editor, region) {
		const doc = editor.editorDocument;
		const line = doc.createElement('div');
		line.appendChild(doc.createElement('br'));
		appendAfterRegionContent(region, line);
		placeCaretAtEnd(editor, line);
	}

	function insertRegionNode(editor, node) {
		const parentRegion = findSelectionRegion(editor);
		if (parentRegion) {
			appendAfterRegionContent(parentRegion, node);
		} else {
			editor.selection.insertNode(node);
		}

		// Jodit can leave a stray zero-width text node (caret anchor) between
		// the inserted region and a trailing <br>; skip past those before
		// removing the <br>, otherwise the <br> check below never matches and
		// the invisible text node is left behind, creating an empty extra line.
		const emptyTextPattern = new RegExp('^[\\s\\uFEFF\\u200B]*$');
		let next = node.nextSibling;
		while (next && next.nodeType === 3 && emptyTextPattern.test(next.nodeValue)) {
			const empty = next;
			next = next.nextSibling;
			empty.remove();
		}
		if (next && next.nodeName === 'BR') {
			next.remove();
		}
	}

	function openOptionListDialog(title, onConfirm) {
		const overlay = document.createElement('div');
		overlay.className = 'position-fixed top-0 start-0 w-100 h-100 d-flex align-items-center justify-content-center';
		overlay.style.background = 'rgba(0, 0, 0, 0.5)';
		overlay.style.zIndex = '2000';

		const box = document.createElement('div');
		box.className = 'bg-white rounded shadow p-3';
		box.style.width = '360px';
		overlay.appendChild(box);

		const heading = document.createElement('h6');
		heading.className = 'mb-3';
		heading.textContent = title;
		box.appendChild(heading);

		const rowsContainer = document.createElement('div');
		rowsContainer.className = 'mb-2';
		box.appendChild(rowsContainer);

		function addRow(value) {
			const row = document.createElement('div');
			row.className = 'd-flex gap-2 mb-2';

			const input = document.createElement('input');
			input.type = 'text';
			input.className = 'form-control form-control-sm';
			input.placeholder = 'Option';
			input.value = value || '';
			input.addEventListener('keydown', (e) => {
				if (e.key !== 'Enter') return;
				e.preventDefault();
				addRow('').focus();
			});

			const removeBtn = document.createElement('button');
			removeBtn.type = 'button';
			removeBtn.className = 'btn btn-outline-secondary btn-sm';
			removeBtn.textContent = '×';
			removeBtn.addEventListener('click', () => {
				if (rowsContainer.children.length > 1) row.remove();
			});

			row.appendChild(input);
			row.appendChild(removeBtn);
			rowsContainer.appendChild(row);
			return input;
		}

		addRow('');
		addRow('');

		const addBtn = document.createElement('button');
		addBtn.type = 'button';
		addBtn.className = 'btn btn-link btn-sm p-0 mb-3';
		addBtn.textContent = '+ Add option';
		addBtn.addEventListener('click', () => addRow('').focus());
		box.appendChild(addBtn);

		const actions = document.createElement('div');
		actions.className = 'd-flex justify-content-end gap-2';

		const cancelBtn = document.createElement('button');
		cancelBtn.type = 'button';
		cancelBtn.className = 'btn btn-outline-secondary btn-sm';
		cancelBtn.textContent = 'Cancel';

		const okBtn = document.createElement('button');
		okBtn.type = 'button';
		okBtn.className = 'btn btn-primary btn-sm';
		okBtn.textContent = 'OK';

		actions.appendChild(cancelBtn);
		actions.appendChild(okBtn);
		box.appendChild(actions);

		function close() {
			overlay.remove();
		}

		cancelBtn.addEventListener('click', close);
		overlay.addEventListener('click', (e) => {
			if (e.target === overlay) close();
		});

		okBtn.addEventListener('click', () => {
			const options = [...rowsContainer.querySelectorAll('input')]
				.map((el) => el.value.trim())
				.filter((v) => v.length > 0);
			close();
			if (options.length > 0) onConfirm(options);
		});

		document.body.appendChild(overlay);
		rowsContainer.querySelector('input').focus();
	}

	const dcRegionTextButton = {
		name: 'dcRegionText',
		text: 'Mark Input Region',
		tooltip: 'Mark the selected part as a user input region (Ctrl+Alt+R)',
		exec: (editor) => {
			const attributes = {
				class: 'dc-region',
				'data-dc-region': 'text',
				contenteditable: 'true',
				tabindex: '0'
			};

			if (editor.selection.isCollapsed()) {
				const span = editor.createInside.element('span');
				Object.entries(attributes).forEach(([key, value]) => span.setAttribute(key, value));
				span.appendChild(editor.editorDocument.createTextNode('​'));
				insertRegionNode(editor, span);
			} else {
				editor.selection.commitStyle({ element: 'span', attributes });
			}

			editor.synchronizeValues();
		}
	};

	const dcRegionImageButton = {
		name: 'dcRegionImage',
		text: 'Mark Image Region',
		tooltip: 'Insert a region where the user can paste an image (Ctrl+Alt+M)',
		exec: (editor) => {
			const div = editor.createInside.element('div');
			div.className = 'dc-region-image';
			div.setAttribute('data-dc-region', 'image');
			div.setAttribute('tabindex', '0');
			const placeholder = editor.createInside.element('span');
			placeholder.className = 'dc-region-placeholder';
			placeholder.appendChild(editor.editorDocument.createTextNode('Click, then paste an image with Ctrl+V'));
			div.appendChild(placeholder);
			insertRegionNode(editor, div);
			editor.synchronizeValues();
		}
	};

	const dcRegionCheckboxButton = {
		name: 'dcRegionCheckbox',
		text: 'Insert Checkbox',
		tooltip: 'Insert a checkbox item (Ctrl+Alt+K)',
		exec: (editor) => {
			editor.prompt('Enter the checkbox label', 'Checkbox', (label) => {
				if (!label || label.trim() === '') return;

				const wrapper = editor.createInside.element('span');
				wrapper.className = 'dc-region-checkbox';
				wrapper.setAttribute('data-dc-region', 'checkbox');

				const labelEl = editor.createInside.element('label');
				const input = editor.createInside.element('input');
				input.type = 'checkbox';
				input.value = label;
				labelEl.appendChild(input);
				labelEl.appendChild(editor.editorDocument.createTextNode(' ' + label));
				wrapper.appendChild(labelEl);

				insertRegionNode(editor, wrapper);
				editor.synchronizeValues();
			}, 'Option');
		}
	};

	const dcRegionSelectButton = {
		name: 'dcRegionSelect',
		text: 'Insert Dropdown',
		tooltip: 'Insert a dropdown selection list (Ctrl+Alt+L)',
		exec: (editor) => {
			openOptionListDialog('Dropdown Options', (options) => {
				const select = editor.createInside.element('select');
				select.className = 'dc-region-select';
				select.setAttribute('data-dc-region', 'select');
				select.setAttribute('tabindex', '0');

				const emptyOption = editor.createInside.element('option');
				emptyOption.value = '';
				emptyOption.appendChild(editor.editorDocument.createTextNode('Select'));
				select.appendChild(emptyOption);

				options.forEach((label) => {
					const opt = editor.createInside.element('option');
					opt.value = label;
					opt.appendChild(editor.editorDocument.createTextNode(label));
					select.appendChild(opt);
				});

				insertRegionNode(editor, select);
				editor.synchronizeValues();
			});
		}
	};

	function findSelectedTable(editor) {
		const sel = editor.editorWindow.getSelection();
		if (!sel || !sel.rangeCount) return null;
		const range = sel.getRangeAt(0);
		const node = range.commonAncestorContainer;
		const el = node.nodeType === 3 ? node.parentElement : node;
		return el ? el.closest('table') : null;
	}

	function isBlankParagraph(node) {
		if (!node || node.nodeType !== 1 || node.tagName !== 'P') return false;
		return node.textContent.trim() === '' && !node.querySelector('img, table, input, select, textarea');
	}

	function removeBlankParagraphsBetweenJoinedTables(table) {
		let next = table.nextSibling;
		const blankAfter = [];
		while (isBlankParagraph(next)) {
			blankAfter.push(next);
			next = next.nextSibling;
		}
		if (next && next.nodeType === 1 && next.matches('table.dc-joined-table')) {
			blankAfter.forEach((node) => node.remove());
		}

		let prev = table.previousSibling;
		const blankBefore = [];
		while (isBlankParagraph(prev)) {
			blankBefore.push(prev);
			prev = prev.previousSibling;
		}
		if (prev && prev.nodeType === 1 && prev.matches('table.dc-joined-table')) {
			blankBefore.forEach((node) => node.remove());
		}
	}

	const dcJoinedTableButton = {
		name: 'dcJoinedTable',
		text: 'Join Table',
		tooltip: 'Toggle no-gap spacing for the selected table (Ctrl+Alt+T)',
		exec: (editor) => {
			const table = findSelectedTable(editor);
			if (!table) {
				editor.message.info('Place the cursor inside a table first.');
				return;
			}
			table.classList.toggle('dc-joined-table');
			if (table.classList.contains('dc-joined-table')) {
				removeBlankParagraphsBetweenJoinedTables(table);
			}
			editor.synchronizeValues();
		}
	};

	function setUpRegionDeleteButton(editorInstance) {
		const deleteBtn = document.createElement('button');
		deleteBtn.type = 'button';
		deleteBtn.textContent = '×';
		deleteBtn.setAttribute('aria-label', 'Delete region');
		Object.assign(deleteBtn.style, {
			position: 'absolute',
			display: 'none',
			alignItems: 'center',
			justifyContent: 'center',
			width: '20px',
			height: '20px',
			padding: '0',
			border: 'none',
			borderRadius: '50%',
			background: '#dc2626',
			color: '#fff',
			fontSize: '13px',
			lineHeight: '1',
			cursor: 'pointer',
			zIndex: '1000'
		});
		document.body.appendChild(deleteBtn);

		let activeRegion = null;

		function positionButton(el) {
			const iframeRect = editorInstance.iframe.getBoundingClientRect();
			const elRect = el.getBoundingClientRect();
			deleteBtn.style.top = (iframeRect.top + elRect.top + window.scrollY - 10) + 'px';
			deleteBtn.style.left = (iframeRect.left + elRect.right + window.scrollX - 10) + 'px';
			deleteBtn.style.display = 'flex';
		}

		editorInstance.editorDocument.addEventListener('click', (e) => {
			const region = e.target.closest('[data-dc-region]');
			if (region) {
				activeRegion = region;
				positionButton(region);
			} else {
				activeRegion = null;
				deleteBtn.style.display = 'none';
			}
		});

		deleteBtn.addEventListener('mousedown', (e) => e.preventDefault());
		deleteBtn.addEventListener('click', (e) => {
			e.stopPropagation();
			if (!activeRegion) return;
			activeRegion.remove();
			activeRegion = null;
			deleteBtn.style.display = 'none';
			editorInstance.synchronizeValues();
		});
	}

	function setUpImageRegionResizeHandle(editorInstance) {
		const resizeHandle = document.createElement('button');
		resizeHandle.type = 'button';
		resizeHandle.setAttribute('aria-label', 'Resize image region');
		Object.assign(resizeHandle.style, {
			position: 'absolute',
			display: 'none',
			width: '44px',
			height: '12px',
			padding: '0',
			border: '1px solid #fff',
			borderRadius: '999px',
			background: '#2563eb',
			boxShadow: '0 1px 3px rgba(0, 0, 0, 0.25)',
			cursor: 'ns-resize',
			zIndex: '1000'
		});
		document.body.appendChild(resizeHandle);

		let activeRegion = null;

		function positionHandle(el) {
			const iframeRect = editorInstance.iframe.getBoundingClientRect();
			const elRect = el.getBoundingClientRect();
			resizeHandle.style.top = (iframeRect.top + elRect.bottom + window.scrollY - 6) + 'px';
			resizeHandle.style.left = (iframeRect.left + elRect.left + window.scrollX + (elRect.width / 2) - 22) + 'px';
			resizeHandle.style.display = 'block';
		}

		function hideHandle() {
			activeRegion = null;
			resizeHandle.style.display = 'none';
		}

		function refreshHandle() {
			if (!activeRegion || !activeRegion.isConnected) {
				hideHandle();
				return;
			}
			positionHandle(activeRegion);
		}

		editorInstance.editorDocument.addEventListener('click', (e) => {
			const region = e.target.closest('.dc-region-image[data-dc-region="image"]');
			if (region) {
				activeRegion = region;
				positionHandle(region);
			} else {
				hideHandle();
			}
		});

		resizeHandle.addEventListener('pointerdown', (e) => {
			e.preventDefault();
			e.stopPropagation();
			if (!activeRegion) return;
			resizeHandle.setPointerCapture(e.pointerId);

			const startY = e.clientY;
			const startHeight = activeRegion.getBoundingClientRect().height;

			function onMove(ev) {
				const nextHeight = Math.max(60, Math.round(startHeight + (ev.clientY - startY)));
				activeRegion.style.height = '';
				activeRegion.style.minHeight = nextHeight + 'px';
				positionHandle(activeRegion);
			}

			function onUp(ev) {
				resizeHandle.removeEventListener('pointermove', onMove);
				resizeHandle.removeEventListener('pointerup', onUp);
				resizeHandle.removeEventListener('pointercancel', onUp);
				if (resizeHandle.hasPointerCapture(ev.pointerId)) {
					resizeHandle.releasePointerCapture(ev.pointerId);
				}
				editorInstance.synchronizeValues();
			}

			resizeHandle.addEventListener('pointermove', onMove);
			resizeHandle.addEventListener('pointerup', onUp);
			resizeHandle.addEventListener('pointercancel', onUp);
		});

		window.addEventListener('scroll', refreshHandle);
		window.addEventListener('resize', refreshHandle);
		editorInstance.editorWindow.addEventListener('scroll', refreshHandle);
	}

	function alignEditorDocument(editorInstance) {
		const body = editorInstance.editorDocument && editorInstance.editorDocument.body;
		if (!body) return;
		body.classList.add('dc-editor-doc');
	}

	function setUpRegionEnterExit(editorInstance) {
		editorInstance.editorDocument.addEventListener('keydown', (e) => {
			if (e.key !== 'Enter' || e.shiftKey) return;
			const region = e.target.closest && e.target.closest('[data-dc-region]');
			if (!region) return;
			e.preventDefault();
			appendPlainTextLineAfterRegion(editorInstance, region);
			editorInstance.synchronizeValues();
		});
	}

	// Ctrl/Cmd+Alt rather than Ctrl/Cmd+Shift: Chrome reserves Ctrl+Shift+C/I/J
	// for DevTools on Windows/Linux, and separately Cmd+Option+I/J/C/U on
	// Mac (a completely different letter set — J was picked for Join Table
	// originally and collided with Mac's "open Console" before this comment
	// was updated). Those OS/browser-level bindings win over anything a page
	// registers, so a hotkey landing on one would silently never fire. Avoid
	// C/I/J/U in both letter sets when adding more of these.
	function registerRegionHotkeys(editorInstance) {
		const bindings = [
			{ name: 'dcRegionText', exec: dcRegionTextButton.exec, keys: ['ctrl+alt+r', 'cmd+alt+r'] },
			{ name: 'dcRegionImage', exec: dcRegionImageButton.exec, keys: ['ctrl+alt+m', 'cmd+alt+m'] },
			{ name: 'dcRegionCheckbox', exec: dcRegionCheckboxButton.exec, keys: ['ctrl+alt+k', 'cmd+alt+k'] },
			{ name: 'dcRegionSelect', exec: dcRegionSelectButton.exec, keys: ['ctrl+alt+l', 'cmd+alt+l'] },
			{ name: 'dcJoinedTable', exec: dcJoinedTableButton.exec, keys: ['ctrl+alt+t', 'cmd+alt+t'] }
		];
		bindings.forEach(({ name, exec, keys }) => {
			editorInstance.registerCommand(name, { exec: () => exec(editorInstance), hotkeys: keys });
		});
	}

	// Excel keeps its own absolute px table width and, when pasted, carries it
	// over as a percentage relative to whatever it was copied from (often wider
	// than our 174mm content area) plus a legacy centering-hack margin. Left
	// untouched this renders the table past the page edge (width > 100%,
	// negative margin). Only touch tables that are actually overflowing — a
	// deliberately narrower pasted/authored table (e.g. width: 60%) is left alone.
	function normalizePastedTableGeometry(editorInstance) {
		const doc = editorInstance.editorDocument;
		if (!doc) return;

		let changed = false;
		doc.querySelectorAll('table').forEach((table) => {
			const widthOverflows = /%$/.test(table.style.width) && parseFloat(table.style.width) > 100;
			const marginPullsLeft = parseFloat(table.style.marginLeft) < 0;
			const marginPullsRight = parseFloat(table.style.marginRight) < 0;
			if (!widthOverflows && !marginPullsLeft && !marginPullsRight) return;

			table.style.removeProperty('width');
			table.style.removeProperty('margin-left');
			table.style.removeProperty('margin-right');
			table.removeAttribute('width');

			// Clearing the outer <table>'s own width/margin alone isn't enough:
			// Excel also puts `white-space: nowrap` on every cell, and under
			// table-layout:auto (forced by the :has(td[colspan]) rule for any
			// table this size) that alone re-expands the table back out to
			// content width — measured to make no difference whether the
			// legacy px `width` attribute on each cell is also cleared, so we
			// leave that one alone and only touch what's actually load-bearing.
			table.querySelectorAll('*').forEach((el) => {
				if (el.style.whiteSpace === 'nowrap') el.style.whiteSpace = '';
			});

			changed = true;
		});

		if (changed) editorInstance.synchronizeValues();
	}

	Jodit.make('#dcContentEditor', {
		language: 'en',
		height: 500,
		iframe: true,
		iframeCSSLinks: ['/css/document.css'],
		allowResizeTags: new Set(['table', 'td', 'th', 'img']),
		buttons: [
			'source', '|',
			'bold', 'italic', 'underline', '|',
			'ul', 'ol', '|',
			'font', 'fontsize', 'brush', '|',
			'align', '|',
			'table', 'image', '|',
			dcJoinedTableButton, dcRegionTextButton, dcRegionImageButton, dcRegionCheckboxButton, dcRegionSelectButton, '|',
			'undo', 'redo', 'eraser'
		],
		events: {
			afterInit: (editorInstance) => {
				alignEditorDocument(editorInstance);
				setUpRegionDeleteButton(editorInstance);
				setUpImageRegionResizeHandle(editorInstance);
				setUpRegionEnterExit(editorInstance);
				registerRegionHotkeys(editorInstance);
				// Bound here (rather than as a top-level `events.afterPaste` key)
				// so the closure always has the real editor instance — Jodit's
				// own afterPaste listeners receive the native paste DOM event as
				// their argument, not the editor.
				editorInstance.events.on('afterPaste', () => normalizePastedTableGeometry(editorInstance));
				// Also run once on load: templates saved before this fix existed
				// still carry the broken width/margin in their stored content_html,
				// and only get fixed once someone opens and re-saves them.
				normalizePastedTableGeometry(editorInstance);
			}
		}
	});
})();
