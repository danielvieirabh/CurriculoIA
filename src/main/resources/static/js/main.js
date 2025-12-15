document.addEventListener('DOMContentLoaded', () => {

    // Referências do DOM
    const uploadForm = document.getElementById('upload-form');
    const fileInput = document.getElementById('file-upload');
    const uploadText = document.getElementById('upload-text');
    const dropZone = document.getElementById('drop-zone');
    const uploadSection = document.getElementById('upload-section');
    const resultSection = document.getElementById('result-section');
    const loadingSection = document.getElementById('loading-section');
    const analysisResult = document.getElementById('analysis-result');
    const backToUploadBtn = document.getElementById('back-to-upload');
    const downloadPdfBtn = document.getElementById('download-pdf-btn');

    let selectedFile = null;
    let currentAnalysisText = ''; // Armazena o texto da análise em Markdown

    // Atualiza UI quando arquivo é selecionado
    fileInput.addEventListener('change', (event) => {
        handleFiles(event.target.files);
    });

    // Função para processar arquivo visualmente
    function handleFiles(files) {
        if (files.length > 0) {
            selectedFile = files[0];
            dropZone.querySelector('.icon-container').innerHTML = '<i class="fa-solid fa-file-circle-check" style="color: #27c93f;"></i>';
            uploadText.innerHTML = `Arquivo pronto: <span style="color:var(--neon-cyan)">${selectedFile.name}</span>`;
            dropZone.style.borderColor = '#27c93f';
        }
    }

    // Envio do formulário
    uploadForm.addEventListener('submit', async (event) => {
        event.preventDefault();
        if (!selectedFile) {
            alert('⚠ PROTOCOLO INTERROMPIDO: Nenhum arquivo detectado.');
            return;
        }

        switchSection('loading');

        const formData = new FormData();
        formData.append('file', selectedFile);

        try {
            const response = await fetch('/api/analisar', {
                method: 'POST',
                body: formData,
            });

            const result = await response.json();

            if (response.ok) {
                currentAnalysisText = result.analise; // Salva o texto original
                analysisResult.innerHTML = marked.parse(currentAnalysisText); // Renderiza o Markdown
                switchSection('result');
            } else {
                analysisResult.innerHTML = `<div class="error-msg">❌ ERRO NO SISTEMA: ${result.analise || 'Falha na análise'}</div>`;
                switchSection('result');
            }

        } catch (error) {
            console.error('System Error:', error);
            analysisResult.innerHTML = `<div class="error-msg">❌ FALHA DE CONEXÃO COM O SERVIDOR</div>`;
            switchSection('result');
        }
    });

    // Botão Voltar
    backToUploadBtn.addEventListener('click', () => {
        resetInterface();
    });

    // Botão Baixar PDF
    downloadPdfBtn.addEventListener('click', async () => {
        if (!currentAnalysisText) return;

        try {
            const response = await fetch('/api/gerar-pdf', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ texto: currentAnalysisText }),
            });

            if (response.ok) {
                const blob = await response.blob();
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.style.display = 'none';
                a.href = url;
                a.download = 'analise_curriculo.pdf';
                document.body.appendChild(a);
                a.click();
                window.URL.revokeObjectURL(url);
                a.remove();
            } else {
                alert('Erro ao gerar o PDF.');
            }
        } catch (error) {
            console.error('PDF Download Error:', error);
            alert('Falha na conexão ao tentar gerar o PDF.');
        }
    });

    // Função Auxiliar para Troca de Seções
    function switchSection(sectionName) {
        uploadSection.classList.add('hidden');
        loadingSection.classList.add('hidden');
        resultSection.classList.add('hidden');

        if(sectionName === 'loading') loadingSection.classList.remove('hidden');
        if(sectionName === 'result') resultSection.classList.remove('hidden');
        if(sectionName === 'upload') uploadSection.classList.remove('hidden');
    }

    function resetInterface() {
        switchSection('upload');
        selectedFile = null;
        currentAnalysisText = '';
        fileInput.value = '';
        uploadText.textContent = 'Arraste o arquivo para a zona de carga';
        dropZone.querySelector('.icon-container').innerHTML = '<i class="fa-solid fa-cloud-arrow-up"></i>';
        dropZone.style.borderColor = 'rgba(59, 130, 246, 0.3)';
        analysisResult.innerHTML = '';
    }

    // Drag and Drop Effects
    dropZone.addEventListener('dragover', (e) => {
        e.preventDefault();
        dropZone.classList.add('drag-over');
    });

    dropZone.addEventListener('dragleave', () => {
        dropZone.classList.remove('drag-over');
    });

    dropZone.addEventListener('drop', (e) => {
        e.preventDefault();
        dropZone.classList.remove('drag-over');
        handleFiles(e.dataTransfer.files);
    });
});