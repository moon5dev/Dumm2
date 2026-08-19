(function () {
	const textarea = document.getElementById('dcContentEditor');
	if (!textarea || typeof Jodit === 'undefined') return;

	function insertRegionNode(editor, node) {
		editor.selection.insertNode(node);
		if (node.nextSibling && node.nextSibling.nodeName === 'BR') {
			node.nextSibling.remove();
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
		tooltip: 'Mark the selected part as a user input region',
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
		tooltip: 'Insert a region where the user can paste an image',
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
		tooltip: 'Insert a checkbox item',
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
		tooltip: 'Insert a dropdown selection list',
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

	Jodit.make('#dcContentEditor', {
		language: 'en',
		height: 500,
		iframe: true,
		iframeCSSLinks: ['/css/document.css'],
		allowResizeTags: new Set(['table', 'img']),
		buttons: [
			'source', '|',
			'bold', 'italic', 'underline', '|',
			'ul', 'ol', '|',
			'font', 'fontsize', 'brush', '|',
			'align', '|',
			'table', 'image', '|',
			dcRegionTextButton, dcRegionImageButton, dcRegionCheckboxButton, dcRegionSelectButton, '|',
			'undo', 'redo', 'eraser'
		],
		events: {
			afterInit: setUpRegionDeleteButton
		}
	});
})();
