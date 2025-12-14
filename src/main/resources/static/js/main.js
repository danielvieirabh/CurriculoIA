document.addEventListener('DOMContentLoaded', () => {
    const uploadForm = document.getElementById('upload-form');
    const fileInput = document.getElementById('file-upload');
    const uploadText = document.getElementById('upload-text');
    const uploadSection = document.getElementById('upload-section');
    const resultSection = document.getElementById('result-section');
    const loadingSection = document.getElementById('loading-section');
    const analysisResult = document.getElementById('analysis-result');
    const backToUploadBtn = document.getElementById('back-to-upload');

    let selectedFile = null;

    fileInput.addEventListener('change', (event) => {
        selectedFile = event.target.files[0];
        if (selectedFile) {
            uploadText.textContent = selectedFile.name;
        }
    });

    uploadForm.addEventListener('submit', async (event) => {
        event.preventDefault();
        if (!selectedFile) {
            alert('Por favor, selecione um arquivo primeiro.');
            return;
        }

        showLoading();

        const formData = new FormData();
        formData.append('file', selectedFile);

        try {
            const response = await fetch('/api/analisar', {
                method: 'POST',
                body: formData,
            });

            const result = await response.json();

            if (response.ok) {
                // Usa a biblioteca 'marked' para converter Markdown em HTML
                analysisResult.innerHTML = marked.parse(result.analise);
                showResults();
            } else {
                analysisResult.innerHTML = `<p class="error"><strong>Erro:</strong> ${result.analise || 'Não foi possível analisar o currículo.'}</p>`;
                showResults();
            }

        } catch (error) {
            console.error('Erro na requisição:', error);
            analysisResult.innerHTML = `<p class="error"><strong>Erro de comunicação com o servidor.</strong> Verifique o console para mais detalhes.</p>`;
            showResults();
        }
    });

    backToUploadBtn.addEventListener('click', () => {
        resetToUpload();
    });

    function showLoading() {
        uploadSection.classList.add('hidden');
        resultSection.classList.add('hidden');
        loadingSection.classList.remove('hidden');
    }

    function showResults() {
        loadingSection.classList.add('hidden');
        uploadSection.classList.add('hidden');
        resultSection.classList.remove('hidden');
    }



    function resetToUpload() {
        resultSection.classList.add('hidden');
        loadingSection.classList.add('hidden');
        uploadSection.classList.remove('hidden');

        uploadText.textContent = 'Arraste e solte o currículo aqui';
        selectedFile = null;
        fileInput.value = ''; // Limpa o input de arquivo
        analysisResult.innerHTML = '';
    }

    // Funcionalidade de Arrastar e Soltar
    const dragDropZone = document.querySelector('.drag-drop-zone');

    dragDropZone.addEventListener('dragover', (event) => {
        event.preventDefault();
        dragDropZone.classList.add('drag-over');
    });

    dragDropZone.addEventListener('dragleave', () => {
        dragDropZone.classList.remove('drag-over');
    });

    dragDropZone.addEventListener('drop', (event) => {
        event.preventDefault();
        dragDropZone.classList.remove('drag-over');
        const files = event.dataTransfer.files;
        if (files.length > 0) {
            fileInput.files = files;
            selectedFile = files[0];
            uploadText.textContent = selectedFile.name;
        }
    });
});
