const img = document.getElementById('img');
const fpsEl = document.getElementById('fps');
const resEl = document.getElementById('res');
const sampleBase64 = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGNgYAAAAAMAASsJTYQAAAAASUVORK5CYII=";
function init() {
  img.src = sampleBase64;
  img.onload = () => {
    resEl.textContent = `${img.naturalWidth}x${img.naturalHeight}`;
    fpsEl.textContent = "0 (static sample)";
  };
}
init();
