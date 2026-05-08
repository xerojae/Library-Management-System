let currentRole = "";

// Switch between Login and Register forms
function toggleAuthMode() {
    const title = document.getElementById('auth-title');
    const isLogin = title.innerText === "Login";
    title.innerText = isLogin ? "Register" : "Login";
    document.getElementById('register-fields').style.display = isLogin ? "block" : "none";
}

// Show librarian code field only if librarian role is selected
function toggleCodeField() {
    const isLib = document.getElementById('userRole').value === "librarian";
    document.getElementById('libCode').style.display = isLib ? "block" : "none";
}

// Handle Login/Register and enter the system
function handleAuth() {
    const role = document.getElementById('userRole').value;
    currentRole = role;
    
    // Switch views
    document.getElementById('auth-page').style.display = "none";
    document.getElementById('main-system').style.display = "block";
    
    // Show role-specific navigation
    document.getElementById('nav-librarian').style.display = (role === "librarian") ? "block" : "none";
    document.getElementById('nav-student').style.display = (role === "student") ? "block" : "none";

    if (role === "librarian") refreshCode();
    doSearch(); // Load books immediately
}

// Librarian only: Fetch the 5-minute code
async function refreshCode() {
    const response = await fetch('/api/librarian-code');
    const data = await response.json();
    document.getElementById('displayCode').innerText = data.code;
}

// Search interface logic
async function doSearch() {
    const query = document.getElementById('q').value.toLowerCase();
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

function showSection(id) {
    document.querySelectorAll('.section').forEach(s => s.style.display = "none");
    document.getElementById(id).style.display = "block";
}

function logout() { location.reload(); }