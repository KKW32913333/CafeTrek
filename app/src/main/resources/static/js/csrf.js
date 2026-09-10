// Reads the CSRF token/header name from <meta> tags (set in fragments/nav.html)
// and returns fetch-ready headers. Every AJAX POST/DELETE in this app should
// spread this in, since Spring Security's CSRF filter rejects state-changing
// requests without it.
function csrfHeaders(extra = {}) {
  const token = document.querySelector('meta[name="_csrf"]')?.content;
  const header = document.querySelector('meta[name="_csrf_header"]')?.content;
  return token && header ? { ...extra, [header]: token } : extra;
}
