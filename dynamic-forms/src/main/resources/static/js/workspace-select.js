(function () {
	const container = document.querySelector('[data-base-url]');
	const baseUrl = container.dataset.baseUrl;
	const selected = new Set((container.dataset.selectedIds || '').split(',').filter(Boolean));

	document.querySelectorAll('input[name="templateId"]').forEach((checkbox) => {
		checkbox.addEventListener('change', () => {
			if (checkbox.checked) {
				selected.add(checkbox.value);
			} else {
				selected.delete(checkbox.value);
			}
		});
	});

	document.querySelectorAll('.dc-category-link').forEach((btn) => {
		btn.addEventListener('click', () => {
			const params = new URLSearchParams();
			const categoryId = btn.dataset.categoryId;
			if (categoryId) params.set('categoryId', categoryId);
			selected.forEach((id) => params.append('selected', id));

			window.location.href = baseUrl + (params.toString() ? '?' + params.toString() : '');
		});
	});

	const startBtn = document.getElementById('dcStartBtn');
	if (startBtn) {
		startBtn.addEventListener('click', () => {
			if (selected.size === 0) {
				alert('Select at least one template first.');
				return;
			}
			const params = new URLSearchParams();
			selected.forEach((id) => params.append('templateId', id));
			window.location.href = baseUrl + '/order?' + params.toString();
		});
	}
})();
