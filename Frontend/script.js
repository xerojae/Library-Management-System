let currentRole = "";
let dynamicCuratedLists = {}; // NEW: Store the dynamic lists from the server

// 1. SESSION & AUTHENTICATION

window.onload = () => {
    const savedRole = localStorage.getItem('activeRole');
    if (savedRole) {
        enterSystem(savedRole);
    }
};

function logout() {
    localStorage.removeItem('activeRole');
    localStorage.removeItem('activeUserIndex');
    location.reload(); 
}

function toggleAuthMode() {
    const title = document.getElementById('auth-title');
    const btn = document.getElementById('authBtn');
    const msg = document.getElementById('toggleMsg');
    const isLogin = title.innerText === "Library Login";

    title.innerText = isLogin ? "Library Registration" : "Library Login";
    btn.innerText = isLogin ? "Register" : "Login";
    msg.innerText = isLogin ? "Already have an account? Switch to Login" : "Don't have an account? Switch to Register";
    
    document.getElementById('reg-fields').style.display = isLogin ? "block" : "none"; 
}

function toggleCodeInput() {
    const isLib = document.getElementById('userRole').value === "librarian";
    document.getElementById('libCode').style.display = isLib ? "block" : "none";
}

async function handleAuth() {
    const email = document.getElementById('email').value;
    const pass = document.getElementById('pass').value;
    const isReg = document.getElementById('reg-fields').style.display === "block";

    if (!email || !pass) return alert("Please enter both email and password.");

    if (isReg) {
        const role = document.getElementById('userRole').value;
        const libCode = document.getElementById('libCode').value;

        if (role === 'librarian') {
            const res = await fetch('/api/librarian-code');
            const data = await res.json();
            if (libCode !== data.code) return alert("Invalid Librarian Security Code.");
        }

        const res = await fetch('/api/register', { method: 'POST', body: `${email}|${pass}|${role}` });
        const result = await res.json();
        
        if (res.ok) {
            localStorage.setItem('activeRole', role);
            localStorage.setItem('activeUserIndex', result.userIndex);
            enterSystem(role);
        } else {
            alert(result.message); 
        }

    } else {
        const res = await fetch('/api/login', { method: 'POST', body: `${email}|${pass}` });
        const result = await res.json();
        
        if (res.ok) {
            localStorage.setItem('activeRole', result.role);
            localStorage.setItem('activeUserIndex', result.userIndex);
            enterSystem(result.role);
        } else {
            alert(result.message); 
        }
    }
}

function enterSystem(role) {
    currentRole = role;
    document.getElementById('auth-page').style.display = "none";
    document.getElementById('main-system').style.display = "block";
    
    // Manage Navigation Visibility
    document.getElementById('lib-nav').style.display = (role === "librarian") ? "inline-block" : "none";
    document.getElementById('student-nav').style.display = (role === "student") ? "inline-block" : "none";
    document.getElementById('curated-nav').style.display = (role === "student") ? "inline-block" : "none";
    document.getElementById('support-nav').style.display = (role === "student") ? "inline-block" : "none";
    
    // Fetch curated lists for the dropdowns
    fetchCuratedDropdowns();

    if (role === "librarian") {
        refreshCode();
        loadLibrarianData();
    } else {
        loadMyBorrowings(); 
    }
    doSearch(); 
}

// 2. SEARCH & VIEW BOOKS

async function doSearch() {
    const q = document.getElementById('searchInput').value.toLowerCase();
    const res = await fetch('/api/books');
    const books = await res.json();
    const resultsDiv = document.getElementById('book-results');
    
    const isStudent = currentRole === "student";
    let myBorrowedBooks = [];

    // Fetch personal borrowed books to toggle Borrow/Return buttons properly
    if (isStudent) {
        const userIndex = localStorage.getItem('activeUserIndex');
        if (userIndex) {
            const bRes = await fetch(`/api/my-borrowings?userIndex=${userIndex}`);
            if (bRes.ok) {
                myBorrowedBooks = await bRes.json();
            }
        }
    }

    const filtered = books.filter(b => 
        b.name.toLowerCase().includes(q) || b.author.toLowerCase().includes(q)
    );

    resultsDiv.innerHTML = filtered.length ? "" : "<p>No resources found.</p>";
    
    filtered.forEach(b => {
        let actionButtons = `<p><em>Librarian View</em></p>`;
        
        if (isStudent) {
            const hasBorrowed = myBorrowedBooks.includes(b.name);
            if (hasBorrowed) {
                actionButtons = `<button class="secondary-btn" onclick="returnBook('${b.name}')">Return</button>`;
            } else {
                actionButtons = `<button class="primary-btn" onclick="borrowBook('${b.name}')" ${b.brwcopies === 0 ? 'disabled' : ''}>Borrow</button>`;
            }
        }

        resultsDiv.innerHTML += `
            <div class="book-card">
                <h3>${b.name}</h3>
                <p>By ${b.author}</p>
                <p>Stock: ${b.brwcopies}</p>
                <div class="card-actions">
                    ${actionButtons}
                </div>
            </div>`;
    });
}

// 3. STUDENT ACTIONS

async function borrowBook(name) {
    const userIndex = localStorage.getItem('activeUserIndex');
    const res = await fetch('/api/borrow', { method: 'POST', body: `${userIndex}|${name}` });
    const data = await res.json();
    
    if (res.status === 401) { 
        alert(data.message); 
        logout(); 
        return; 
    }
    
    alert(data.message);
    loadMyBorrowings(); 
    doSearch();
    loadCuratedBooks(); // Update stock in curated list if visible
}

async function loadMyBorrowings() {
    const userIndex = localStorage.getItem('activeUserIndex');
    if (!userIndex) return;

    const res = await fetch(`/api/my-borrowings?userIndex=${userIndex}`);
    const borrowed = await res.json();
    const listDiv = document.getElementById('borrowed-list');
    
    if (!listDiv) return;
    
    listDiv.innerHTML = borrowed.length ? borrowed.map(title => `
        <div class="card" style="margin-bottom:10px; border-left: 4px solid var(--bsu-gold, #FFD700);">
            <strong>${title}</strong>
        </div>`).join('') : "<p>No active borrowings.</p>";
}

async function calculateFine() {
    const userIndex = localStorage.getItem('activeUserIndex');
    if (!userIndex) return;

    // Added the userIndex to the URL so the server knows whose books to check
    const res = await fetch(`/api/calculate-fine?userIndex=${userIndex}`);
    const data = await res.json();
    
    document.getElementById('fine-display').innerText = `Balance: $${data.fine.toFixed(2)}`;
    alert("Fine updated.");
}

async function returnBook(name) {
    const userIndex = localStorage.getItem('activeUserIndex');
    const res = await fetch('/api/return-book', { method: 'POST', body: `${userIndex}|${name}` });
    const data = await res.json();
    
    if (res.status === 401) { 
        alert(data.message); 
        logout(); 
        return; 
    }
    
    alert(data.message);
    loadMyBorrowings();
    doSearch();
    loadCuratedBooks(); // Update stock in curated list if visible
}

// 4. LIBRARIAN ACTIONS

async function refreshCode() {
    const res = await fetch('/api/librarian-code?force=true');
    const data = await res.json();
    document.getElementById('displayCode').innerText = data.code;
}

async function addBook() {
    const title = document.getElementById('add-title').value;
    const author = document.getElementById('add-author').value;
    const qty = document.getElementById('add-qty') ? document.getElementById('add-qty').value : 1;
    
    if (!title || !author) return alert("Both Title and Author are required.");
    
    // Pack only the 3 pieces of data together separated by |
    const payload = `${title}|${author}|${qty}`;
    
    await fetch('/api/add-book', { method: 'POST', body: payload });
    alert("Book Added: " + title);
    
    // Clear out the form
    document.getElementById('add-title').value = "";
    document.getElementById('add-author').value = "";
    if(document.getElementById('add-qty')) document.getElementById('add-qty').value = "";
    
    loadLibrarianData();
    doSearch();
}

async function loadInventory() {
    const res = await fetch('/api/books');
    const books = await res.json();
    const invDiv = document.getElementById('inventory-view');
    if (!invDiv) return;

    invDiv.innerHTML = books.map(b => `
        <div class="book-card" style="border-top: 2px solid #333;">
            <h4>${b.name}</h4>
            <p>Author: ${b.author}</p>
            <p>Copies: ${b.brwcopies}</p>
        </div>`).join('');
}

async function loadLibrarianData() {
    loadInventory();

    const resA = await fetch('/api/audit-log');
    const logs = await resA.json();
    document.getElementById('audit-log').innerHTML = logs.map(l => `<p style="margin:2px 0;">• ${l}</p>`).join('');

    const resB = await fetch('/api/view-borrowings');
    const dataB = await resB.json();
    const bView = document.getElementById('borrowings-view');
    if (bView) bView.innerHTML = dataB.borrowings.length ? "" : "No active borrowings.";

    const resO = await fetch('/api/view-orders');
    const dataO = await resO.json();
    const oView = document.getElementById('orders-view');
    if (oView) oView.innerHTML = dataO.orders.length ? "" : "No active orders.";

    const resT = await fetch('/api/view-tickets');
    const tickets = await resT.json();
    const tView = document.getElementById('ticket-view');
    if (tView) {
        tView.innerHTML = tickets.length ? tickets.map(t => `
            <div style="border-bottom: 1px solid #ccc; padding-bottom: 8px; margin-bottom: 8px;">
                <strong style="color: var(--bsu-primary, #000);">${t.user}</strong>: <em>${t.subject}</em>
                <p style="margin: 4px 0; font-size: 0.85rem;">${t.message}</p>
                <button class="secondary-btn" style="padding: 2px 6px; font-size: 0.8rem;" onclick="resolveTicket(${t.id})">Mark as Resolved</button>
            </div>
        `).join('') : "<p>No active support tickets.</p>";
    }
}

async function deleteBook() {
    const title = document.getElementById('delete-title').value;
    await fetch('/api/delete-book', { method: 'POST', body: title });
    alert("Book Removed.");
    loadLibrarianData();
    doSearch();
}

async function deleteAllData() {
    if (confirm("Clear entire database?")) {
        await fetch('/api/delete-all', { method: 'POST' });
        loadLibrarianData();
        doSearch();
    }
}

// 5. NAVIGATION

function showSection(id) {
    document.querySelectorAll('.section').forEach(s => s.style.display = "none");
    document.getElementById(id).style.display = "block";
    
    if (id === 'admin-section') loadLibrarianData();
    if (id === 'student-section') loadMyBorrowings(); 
}

// ==========================================
// 6. CURATED LISTS & SUPPORT FEATURES
// ==========================================

// NEW: Fetches the lists from the server and populates the dropdowns
async function fetchCuratedDropdowns() {
    try {
        const res = await fetch('/api/get-curated-lists');
        if (!res.ok) return;
        
        dynamicCuratedLists = await res.json();
        
        const studentSelect = document.getElementById('course-select');
        const adminSelect = document.getElementById('admin-course-select');
        
        let optionsHtml = '<option value="">-- Select a Course --</option>';
        for (const course in dynamicCuratedLists) {
            optionsHtml += `<option value="${course}">${course}</option>`;
        }
        
        if (studentSelect) studentSelect.innerHTML = optionsHtml;
        if (adminSelect) adminSelect.innerHTML = optionsHtml;
    } catch (e) {
        console.error("Failed to load curated lists", e);
    }
}

// NEW: Tells the server to create a new list
async function createCuratedList() {
    const courseName = document.getElementById('new-list-name').value;
    if (!courseName) return alert("Please enter a course name.");

    const res = await fetch('/api/add-curated-list', { method: 'POST', body: courseName });
    const result = await res.json();
    alert(result.message);

    if (res.ok) {
        document.getElementById('new-list-name').value = "";
        fetchCuratedDropdowns(); // Refresh the dropdown boxes
        loadLibrarianData(); // Refresh audit log
    }
}

// NEW: Tells the server to add a keyword to a list
async function addKeywordToList() {
    const courseName = document.getElementById('admin-course-select').value;
    const keyword = document.getElementById('new-list-keyword').value;

    if (!courseName || !keyword) return alert("Please select a course and enter a keyword.");

    const payload = `${courseName}||${keyword}`;
    const res = await fetch('/api/add-curated-keyword', { method: 'POST', body: payload });
    const result = await res.json();
    alert(result.message);

    if (res.ok) {
        document.getElementById('new-list-keyword').value = "";
        fetchCuratedDropdowns(); // Refresh cache so students see the update immediately
        loadLibrarianData(); // Refresh audit log
    }
}

async function loadCuratedBooks() {
    const course = document.getElementById('course-select').value;
    const resultsDiv = document.getElementById('curated-results');
    
    if (!course) {
        resultsDiv.innerHTML = "<p>Please select a course above.</p>";
        return;
    }

    // Fetch all books from the server
    const res = await fetch('/api/books');
    const allBooks = await res.json();
    
    // Fetch the dynamic keywords from the dictionary we got from the server
    const allowedKeywords = dynamicCuratedLists[course] || [];

    // Filter books to only show those that match the course keywords
    const filteredBooks = allBooks.filter(b => 
        allowedKeywords.some(keyword => b.name.toLowerCase().includes(keyword.toLowerCase()))
    );

    // Render them using the existing book-card CSS class
    resultsDiv.innerHTML = filteredBooks.length ? "" : "<p>No resources found for this course.</p>";
    
    filteredBooks.forEach(b => {
        resultsDiv.innerHTML += `
            <div class="book-card">
                <h3>${b.name}</h3>
                <p>By ${b.author}</p>
                <p>Stock: ${b.brwcopies}</p>
                <div class="card-actions">
                    <button class="primary-btn" onclick="borrowBook('${b.name}')" ${b.brwcopies === 0 ? 'disabled' : ''}>Borrow</button>
                </div>
            </div>`;
    });
}

function submitSupportForm() {
    const subject = document.getElementById('support-subject').value;
    const message = document.getElementById('support-message').value;
    const userIndex = localStorage.getItem('activeUserIndex') || "0";
    
    if (!subject || !message) {
        alert("Please fill out both the subject and message fields.");
        return;
    }
    
    const payload = `${userIndex}||${subject}||${message}`;
    fetch('/api/submit-ticket', { method: 'POST', body: payload })
        .then(res => {
            if (res.ok) {
                alert("Support Ticket Submitted! A librarian will review your request shortly.");
                document.getElementById('support-subject').value = "";
                document.getElementById('support-message').value = "";
            } else {
                alert("There was an error submitting your ticket.");
            }
        });
}

async function resolveTicket(id) {
    const res = await fetch('/api/resolve-ticket', { method: 'POST', body: id.toString() });
    if (res.ok) {
        loadLibrarianData(); 
    }
}