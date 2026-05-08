let currentRole = "";

// Switch between login and register forms
function toggleAuthMode() {
    const isLogin = document.getElementById('auth-title').innerText === "Login";
    document.getElementById('auth-title').innerText = isLogin ? "Register" : "Login";
    document.getElementById('reg-fields').style.display = isLogin ? "block" : "none";
}

// Show code field only for librarian registration
function toggleCodeInput() {
    const isLib = document.getElementById('userRole').value === "librarian";
    document.getElementById('libCode').style.display = isLib ? "block" : "none";
}

// Enter the system and set role
function handleAuth() {
    const role = document.getElementById('userRole').value;
    currentRole = role;
    document.getElementById('auth-page').style.display = "none";
    document.getElementById('main-system').style.display = "block";
    
    if (role === "librarian") {
        document.getElementById('lib-nav').style.display = "inline-block";
        refreshCode(); 
    }
    doSearch();
}

// Fetch the 5-minute code (Librarian side)
async function refreshCode() {
    const response = await fetch('/api/librarian-code');
    const data = await response.json();
    document.getElementById('displayCode').innerText = data.code;
}

// Fetch and search books
async function doSearch() {
    const query = document.getElementById('searchInput').value.toLowerCase();
    const results = document.getElementById('results');
    const response = await fetch('/api/books');
    const bookData = await response.json();

    results.innerHTML = ''; 
    bookData.filter(b => b.name.toLowerCase().includes(query)).forEach(book => {
        results.innerHTML += `
            <div class="book-card">
                <h3>${book.name}</h3>
                <p>By ${book.author}</p>
                <p>Available: ${book.brwcopies}</p>
                <button ${book.brwcopies === 0 ? 'disabled' : ''}>Borrow</button>
            </div>`;
    });
}

// Nav tab switcher
function showSection(id) {
    document.querySelectorAll('.section').forEach(s => s.style.display = "none");
    document.getElementById(id).style.display = "block";
}