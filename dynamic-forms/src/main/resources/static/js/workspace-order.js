(function () {
	const list = document.getElementById('dcOrderList');
	if (!list) return;

	list.addEventListener('click', (e) => {
		const btn = e.target.closest('button');
		if (!btn) return;
		const item = btn.closest('li');

		if (btn.classList.contains('dc-move-up')) {
			const prev = item.previousElementSibling;
			if (prev) list.insertBefore(item, prev);
		} else if (btn.classList.contains('dc-move-down')) {
			const next = item.nextElementSibling;
			if (next) list.insertBefore(next, item);
		}
	});

	const form = document.getElementById('dcOrderForm');
	document.getElementById('dcContinueBtn').addEventListener('click', () => {
		form.innerHTML = '';
		[...list.children].forEach((li) => {
			const input = document.createElement('input');
			input.type = 'hidden';
			input.name = 'templateId';
			input.value = li.dataset.id;
			form.appendChild(input);
		});
		form.submit();
	});
})();
