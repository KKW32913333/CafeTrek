// Star-rating picker for the 記録 form: clicking a star fills it and the
// ones before it, and writes the value into the hidden `rating` input.
document.addEventListener("DOMContentLoaded", () => {
  const picker = document.getElementById("starpicker");
  if (!picker) return;
  const input = document.getElementById("ratingInput");

  function paint(value) {
    picker.querySelectorAll("span").forEach((s) => {
      s.classList.toggle("filled", Number(s.dataset.v) <= value);
    });
  }

  picker.addEventListener("click", (e) => {
    if (e.target.tagName !== "SPAN") return;
    const v = Number(e.target.dataset.v);
    input.value = v;
    paint(v);
  });

  paint(Number(input.value || 0));
});
