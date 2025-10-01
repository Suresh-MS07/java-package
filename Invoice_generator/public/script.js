document.addEventListener('DOMContentLoaded', () => {

    // --- Authentication Logic ---
    const userInfoDiv = document.getElementById('userInfo');
    const usernameDisplay = document.getElementById('usernameDisplay');
    const logoutBtn = document.getElementById('logoutBtn');

    // User ka data fetch karna
    fetch('/api/user')
        .then(response => response.json())
        .then(data => {
            if (data.username) {
                usernameDisplay.textContent = data.username;
                userInfoDiv.style.display = 'block';
            }
        })
        .catch(err => console.error('Error fetching user:', err));

    // Logout button par click event
    logoutBtn.addEventListener('click', () => {
        fetch('/logout', { method: 'POST' })
            .then(response => {
                if (response.ok) {
                    window.location.href = '/login.html';
                }
            })
            .catch(err => console.error('Logout failed:', err));
    });


    // --- Invoice Logic ---
    const addItemBtn = document.getElementById('addItemBtn');
    const itemsTableBody = document.querySelector('#itemsTable tbody');
    const subtotalSpan = document.getElementById('subtotal');
    const taxSpan = document.getElementById('tax');
    const grandTotalSpan = document.getElementById('grandTotal');
    const generateBtn = document.getElementById('generateBtn');

    function createNewRow() {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td><input type="text" class="form-control" placeholder="e.g., Website Design"></td>
            <td><input type="number" class="form-control quantity" value="1" min="0"></td>
            <td><input type="number" class="form-control rate" placeholder="1000" min="0"></td>
            <td class="text-end amount">0.00</td>
            <td class="text-center"><button class="btn btn-danger btn-sm delete-btn">X</button></td>
        `;
        itemsTableBody.appendChild(row);
    }
    
    addItemBtn.addEventListener('click', createNewRow);

    function updateTotals() {
        let subtotal = 0;
        const rows = itemsTableBody.querySelectorAll('tr');

        rows.forEach(row => {
            const quantity = parseFloat(row.querySelector('.quantity').value) || 0;
            const rate = parseFloat(row.querySelector('.rate').value) || 0;
            const amount = quantity * rate;
            row.querySelector('.amount').textContent = amount.toFixed(2);
            subtotal += amount;
        });

        const tax = subtotal * 0.18;
        const grandTotal = subtotal + tax;
        subtotalSpan.textContent = subtotal.toFixed(2);
        taxSpan.textContent = tax.toFixed(2);
        grandTotalSpan.textContent = grandTotal.toFixed(2);
    }

    itemsTableBody.addEventListener('input', updateTotals);
    
    itemsTableBody.addEventListener('click', (event) => {
        if (event.target.classList.contains('delete-btn')) {
            event.target.closest('tr').remove();
            updateTotals();
        }
    });
    
    createNewRow();
    updateTotals();

    function generatePDF() {
        const { jsPDF } = window.jspdf;
        const doc = new jsPDF();

        const companyName = document.getElementById('companyName').value || "Aapki Company";
        const clientName = document.getElementById('clientName').value || "Client ka Naam";
        
        doc.setFont('helvetica', 'bold');
        doc.setFontSize(20);
        doc.text('INVOICE', 105, 20, { align: 'center' });
        
        doc.setFontSize(12);
        doc.text(companyName, 20, 40);
        
        doc.setFont('helvetica', 'normal');
        doc.text('Bill To:', 20, 50);
        doc.setFont('helvetica', 'bold');
        doc.text(clientName, 20, 55);
        
        const invoiceNum = `INV-${Math.floor(1000 + Math.random() * 9000)}`;
        const invoiceDate = new Date().toLocaleDateString();
        doc.setFont('helvetica', 'normal');
        doc.text(`Invoice #: ${invoiceNum}`, 180, 40, { align: 'right' });
        doc.text(`Date: ${invoiceDate}`, 180, 45, { align: 'right' });

        const tableBodyData = [];
        const rows = itemsTableBody.querySelectorAll('tr');
        rows.forEach(row => {
            const itemDescription = row.querySelector('input[type="text"]').value;
            const quantity = row.querySelector('.quantity').value;
            const rate = row.querySelector('.rate').value;
            const amount = row.querySelector('.amount').textContent;
            tableBodyData.push([itemDescription, quantity, rate, amount]);
        });
        
        doc.autoTable({
            head: [['Description', 'Quantity', 'Rate', 'Amount']],
            body: tableBodyData,
            startY: 70,
            headStyles: { fillColor: [22, 160, 133] },
        });

        const finalY = doc.autoTable.previous.finalY;
        const subtotal = document.getElementById('subtotal').textContent;
        const tax = document.getElementById('tax').textContent;
        const grandTotal = document.getElementById('grandTotal').textContent;
        
        doc.setFontSize(12);
        doc.text(`Subtotal: ${subtotal}`, 180, finalY + 10, { align: 'right' });
        doc.text(`Tax (18%): ${tax}`, 180, finalY + 15, { align: 'right' });
        doc.setFont('helvetica', 'bold');
        doc.text(`Grand Total: ${grandTotal}`, 180, finalY + 20, { align: 'right' });

        doc.save(`Invoice-${clientName.replace(/\s/g, '_')}.pdf`);
    }

    generateBtn.addEventListener('click', generatePDF);
});

