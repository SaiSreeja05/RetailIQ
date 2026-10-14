// RetailIQ Web Platform Application JS

let state = {
  products: [],
  customers: [],
  transactions: [],
  reviews: [],
  warehouses: [],
  analytics: null,
  activeEntity: 'products',
  activeView: 'dashboard'
};

let charts = {};

document.addEventListener('DOMContentLoaded', () => {
  initNavigation();
  initGlobalSearch();
  initAlgoTabs();
  initExplorerTabs();
  initBudgetSlider();
  loadAllData();
});

// =========================================================================
// DATA FETCHING & SYNCHRONIZATION
// =========================================================================
async function loadAllData() {
  try {
    const [pRes, cRes, tRes, rRes, wRes, aRes] = await Promise.all([
      fetch('/api/products').then(r => r.json()),
      fetch('/api/customers').then(r => r.json()),
      fetch('/api/transactions').then(r => r.json()),
      fetch('/api/reviews').then(r => r.json()),
      fetch('/api/warehouses').then(r => r.json()),
      fetch('/api/analytics').then(r => r.json())
    ]);

    state.products = pRes;
    state.customers = cRes;
    state.transactions = tRes;
    state.reviews = rRes;
    state.warehouses = wRes;
    state.analytics = aRes;

    renderDashboard();
    renderProducts();
    renderCustomers();
    renderTransactions();
    renderWarehouses();
    renderReviews();
    renderAnalytics();
    renderDataStatus();
    renderExplorer();
  } catch (err) {
    console.error('Error loading data:', err);
  }
}

// =========================================================================
// NAVIGATION
// =========================================================================
function initNavigation() {
  document.querySelectorAll('.nav-item').forEach(item => {
    item.addEventListener('click', () => {
      const target = item.dataset.view;
      switchView(target);
    });
  });
}

function switchView(viewName) {
  state.activeView = viewName;
  document.querySelectorAll('.nav-item').forEach(i => i.classList.remove('active'));
  const activeNavItem = document.querySelector(`.nav-item[data-view="${viewName}"]`);
  if (activeNavItem) activeNavItem.classList.add('active');

  document.querySelectorAll('.view-section').forEach(s => s.classList.remove('active'));
  const targetSection = document.getElementById(`view-${viewName}`);
  if (targetSection) targetSection.classList.add('active');

  // Trigger chart resizes if needed
  if (viewName === 'dashboard' || viewName === 'analytics') {
    setTimeout(() => {
      Object.values(charts).forEach(c => c && c.resize && c.resize());
    }, 50);
  }
}

// =========================================================================
// 1. DASHBOARD
// =========================================================================
function renderDashboard() {
  const a = state.analytics;
  if (!a) return;

  // KPIs
  const kpiContainer = document.getElementById('dashboardKpis');
  kpiContainer.innerHTML = `
    <div class="kpi-card">
      <div class="kpi-title">Total Products</div>
      <div class="kpi-value">${a.totalProducts}</div>
      <div class="kpi-subtext">Active Catalog Items</div>
    </div>
    <div class="kpi-card">
      <div class="kpi-title">Customers</div>
      <div class="kpi-value">${a.totalCustomers}</div>
      <div class="kpi-subtext">Registered Accounts</div>
    </div>
    <div class="kpi-card">
      <div class="kpi-title">Transactions</div>
      <div class="kpi-value">${a.totalTransactions}</div>
      <div class="kpi-subtext">Completed Orders</div>
    </div>
    <div class="kpi-card">
      <div class="kpi-title">Total Revenue</div>
      <div class="kpi-value">${formatCurrency(a.totalRevenue)}</div>
      <div class="kpi-subtext" style="color: #2e7d32;">Lifetime Sales</div>
    </div>
    <div class="kpi-card">
      <div class="kpi-title">Avg Review Rating</div>
      <div class="kpi-value">${a.averageRating.toFixed(2)} ★</div>
      <div class="kpi-subtext">Customer Satisfaction</div>
    </div>
    <div class="kpi-card">
      <div class="kpi-title">Inventory Units</div>
      <div class="kpi-value">${a.totalInventory}</div>
      <div class="kpi-subtext">${a.warehouseUtilization.toFixed(1)}% Capacity Used</div>
    </div>
  `;

  // Dynamic Insights
  const insightsContainer = document.getElementById('dashboardInsights');
  insightsContainer.innerHTML = a.insights.map(ins => `
    <div class="insight-card">
      <span style="font-size: 18px;">💡</span>
      <span>${ins}</span>
    </div>
  `).join('');

  // Top Products Table
  const topTbody = document.querySelector('#topProductsTable tbody');
  topTbody.innerHTML = a.topProducts.map(tp => `
    <tr>
      <td><strong>${tp.product.name}</strong></td>
      <td><span class="badge badge-olive">${tp.product.category}</span></td>
      <td>${tp.quantitySold} units</td>
      <td><strong>${formatCurrency(tp.totalRevenue)}</strong></td>
    </tr>
  `).join('');

  // Low Stock Table
  const lowTbody = document.querySelector('#lowStockTable tbody');
  lowTbody.innerHTML = a.lowStockProducts.map(p => `
    <tr>
      <td>${p.name}</td>
      <td><code>${p.productCode}</code></td>
      <td><span class="badge ${p.stock <= 10 ? 'badge-rose' : 'badge-amber'}">${p.stock} units left</span></td>
      <td>${formatCurrency(p.price)}</td>
    </tr>
  `).join('');

  // Render Charts
  renderDashboardCharts(a);
}

function renderDashboardCharts(a) {
  // Sales Trend Line Chart
  const salesCtx = document.getElementById('salesTrendChart');
  if (salesCtx) {
    if (charts.salesTrend) charts.salesTrend.destroy();
    const dates = Object.keys(a.salesByDate);
    const amounts = Object.values(a.salesByDate);

    charts.salesTrend = new Chart(salesCtx, {
      type: 'line',
      data: {
        labels: dates,
        datasets: [{
          label: 'Revenue (Rs.)',
          data: amounts,
          borderColor: '#556b2f',
          backgroundColor: 'rgba(85, 107, 47, 0.1)',
          fill: true,
          tension: 0.35,
          borderWidth: 2.5,
          pointBackgroundColor: '#556b2f',
          pointRadius: 4
        }]
      },
      options: {
        responsive: true,
        plugins: { legend: { display: false } },
        scales: {
          y: { grid: { color: 'rgba(85, 107, 47, 0.08)' } },
          x: { grid: { display: false } }
        }
      }
    });
  }

  // Category Bar Chart
  const catCtx = document.getElementById('categoryRevenueChart');
  if (catCtx) {
    if (charts.categoryRev) charts.categoryRev.destroy();
    const cats = Object.keys(a.revenueByCategory);
    const revs = Object.values(a.revenueByCategory);

    charts.categoryRev = new Chart(catCtx, {
      type: 'bar',
      data: {
        labels: cats,
        datasets: [{
          label: 'Total Revenue (Rs.)',
          data: revs,
          backgroundColor: '#6b8e23',
          borderRadius: 6
        }]
      },
      options: {
        responsive: true,
        plugins: { legend: { display: false } },
        scales: {
          y: { grid: { color: 'rgba(85, 107, 47, 0.08)' } },
          x: { grid: { display: false } }
        }
      }
    });
  }
}

// =========================================================================
// 2. PRODUCTS
// =========================================================================
function renderProducts() {
  const tbody = document.querySelector('#productsTable tbody');
  const catFilter = document.getElementById('productCategoryFilter');
  const searchInput = document.getElementById('productSearchInput');

  // Populate category filter options
  const cats = [...new Set(state.products.map(p => p.category))].sort();
  const currentCat = catFilter.value;
  catFilter.innerHTML = '<option value="">All Categories</option>' + cats.map(c => `<option value="${c}">${c}</option>`).join('');
  catFilter.value = currentCat;

  const filter = () => {
    const q = searchInput.value.toLowerCase();
    const selectedCat = catFilter.value;

    const filtered = state.products.filter(p => {
      const matchCat = !selectedCat || p.category === selectedCat;
      const matchQ = !q || p.name.toLowerCase().includes(q) || p.productCode.toLowerCase().includes(q) || p.description.toLowerCase().includes(q);
      return matchCat && matchQ;
    });

    tbody.innerHTML = filtered.map(p => `
      <tr onclick="selectProduct(${p.productId})" style="cursor: pointer;">
        <td>#${p.productId}</td>
        <td><code>${p.productCode}</code></td>
        <td><strong>${p.name}</strong></td>
        <td><span class="badge badge-olive">${p.category}</span></td>
        <td>${formatCurrency(p.price)}</td>
        <td>
          <span class="badge ${p.stock <= 10 ? 'badge-rose' : (p.stock <= 20 ? 'badge-amber' : 'badge-olive')}">
            ${p.stock} units
          </span>
        </td>
        <td>${p.rating.toFixed(1)} ★</td>
        <td>
          <button class="btn btn-secondary btn-sm" onclick="event.stopPropagation(); editProduct(${p.productId})">Edit</button>
          <button class="btn btn-danger btn-sm" onclick="event.stopPropagation(); deleteProduct(${p.productId})">Delete</button>
        </td>
      </tr>
    `).join('');
  };

  searchInput.oninput = filter;
  catFilter.onchange = filter;
  filter();

  if (state.products.length > 0) {
    selectProduct(state.products[0].productId);
  }
}

function selectProduct(id) {
  const p = state.products.find(x => x.productId === id);
  if (!p) return;

  const drawer = document.getElementById('productDetailDrawer');
  const txns = state.transactions.filter(t => t.productId === id);
  const unitsSold = txns.reduce((sum, t) => sum + t.quantity, 0);
  const totalRev = txns.reduce((sum, t) => sum + t.totalAmount, 0);
  const reviews = state.reviews.filter(r => r.productId === id);

  drawer.innerHTML = `
    <div class="card-header">
      <span>Product Intelligence</span>
      <span class="badge badge-olive">#${p.productId}</span>
    </div>
    <h3 style="color: var(--olive-dark); font-size: 17px; margin-bottom: 4px;">${p.name}</h3>
    <div style="font-size: 12px; color: var(--text-muted); margin-bottom: 12px;">
      ${p.category} • Code: <code>${p.productCode}</code>
    </div>
    <p style="font-size: 13px; color: var(--text-main); margin-bottom: 16px;">${p.description}</p>
    
    <div style="background: var(--olive-light); padding: 12px; border-radius: var(--radius-sm); border: 1px solid var(--border-color); margin-bottom: 16px;">
      <div style="display: flex; justify-content: space-between; margin-bottom: 6px; font-size: 13px;">
        <span style="color: var(--text-muted);">Unit Price:</span>
        <strong>${formatCurrency(p.price)}</strong>
      </div>
      <div style="display: flex; justify-content: space-between; margin-bottom: 6px; font-size: 13px;">
        <span style="color: var(--text-muted);">Current Stock:</span>
        <strong>${p.stock} units</strong>
      </div>
      <div style="display: flex; justify-content: space-between; margin-bottom: 6px; font-size: 13px;">
        <span style="color: var(--text-muted);">Inventory Valuation:</span>
        <strong>${formatCurrency(p.price * p.stock)}</strong>
      </div>
      <div style="display: flex; justify-content: space-between; font-size: 13px;">
        <span style="color: var(--text-muted);">Sales Performance:</span>
        <strong style="color: var(--olive-primary);">${unitsSold} units (${formatCurrency(totalRev)})</strong>
      </div>
    </div>

    <div style="font-weight: 700; font-size: 13px; margin-bottom: 8px;">Customer Reviews (${reviews.length})</div>
    <div style="max-height: 180px; overflow-y: auto; display: flex; flex-direction: column; gap: 8px;">
      ${reviews.length === 0 ? '<p style="color: var(--text-muted); font-size: 12px;">No reviews yet.</p>' : 
        reviews.map(r => `
          <div style="background: #fafcf7; padding: 8px 10px; border-radius: var(--radius-sm); border: 1px solid var(--border-subtle);">
            <div style="color: #b58900; font-size: 12px; margin-bottom: 2px;">${'★'.repeat(r.rating)}${'☆'.repeat(5-r.rating)}</div>
            <div style="font-size: 12.5px; color: var(--text-main);">${r.reviewText}</div>
          </div>
        `).join('')
      }
    </div>
  `;
}

function openProductModal(prod = null) {
  document.getElementById('productModalTitle').innerText = prod ? `Edit Product #${prod.productId}` : 'Add New Product';
  document.getElementById('pId').value = prod ? prod.productId : '';
  document.getElementById('pName').value = prod ? prod.name : '';
  document.getElementById('pCategory').value = prod ? prod.category : '';
  document.getElementById('pCode').value = prod ? prod.productCode : '';
  document.getElementById('pPrice').value = prod ? prod.price : '';
  document.getElementById('pStock').value = prod ? prod.stock : '';
  document.getElementById('pRating').value = prod ? prod.rating : '5.0';
  document.getElementById('pDesc').value = prod ? prod.description : '';
  document.getElementById('productModal').style.display = 'flex';
}

function editProduct(id) {
  const p = state.products.find(x => x.productId === id);
  if (p) openProductModal(p);
}

async function handleProductSubmit(e) {
  e.preventDefault();
  const data = {
    productId: document.getElementById('pId').value || '0',
    name: document.getElementById('pName').value.trim(),
    category: document.getElementById('pCategory').value.trim(),
    productCode: document.getElementById('pCode').value.trim(),
    price: document.getElementById('pPrice').value,
    stock: document.getElementById('pStock').value,
    rating: document.getElementById('pRating').value,
    description: document.getElementById('pDesc').value.trim()
  };

  const res = await fetch('/api/products', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  }).then(r => r.json());

  if (res.success) {
    closeModal('productModal');
    loadAllData();
  } else {
    alert('Error saving product: ' + (res.error || 'Unknown error'));
  }
}

async function deleteProduct(id) {
  if (confirm(`Delete Product #${id}? This updates products.csv directly.`)) {
    await fetch(`/api/products?id=${id}`, { method: 'DELETE' });
    loadAllData();
  }
}

// =========================================================================
// 3. CUSTOMERS
// =========================================================================
function renderCustomers() {
  const tbody = document.querySelector('#customersTable tbody');
  const searchInput = document.getElementById('customerSearchInput');

  const filter = () => {
    const q = searchInput.value.toLowerCase();
    const filtered = state.customers.filter(c => {
      return !q || c.name.toLowerCase().includes(q) || c.email.toLowerCase().includes(q) || c.phone.includes(q) || c.address.toLowerCase().includes(q);
    });

    tbody.innerHTML = filtered.map(c => `
      <tr onclick="selectCustomer(${c.customerId})" style="cursor: pointer;">
        <td>#${c.customerId}</td>
        <td><strong>${c.name}</strong></td>
        <td>${c.email}</td>
        <td>${c.phone}</td>
        <td>${c.address}</td>
        <td>
          <button class="btn btn-secondary btn-sm" onclick="event.stopPropagation(); editCustomer(${c.customerId})">Edit</button>
          <button class="btn btn-danger btn-sm" onclick="event.stopPropagation(); deleteCustomer(${c.customerId})">Delete</button>
        </td>
      </tr>
    `).join('');
  };

  searchInput.oninput = filter;
  filter();

  if (state.customers.length > 0) {
    selectCustomer(state.customers[0].customerId);
  }
}

function selectCustomer(id) {
  const c = state.customers.find(x => x.customerId === id);
  if (!c) return;

  const drawer = document.getElementById('customerDetailDrawer');
  const txns = state.transactions.filter(t => t.customerId === id);
  const totalSpend = txns.reduce((sum, t) => sum + t.totalAmount, 0);

  drawer.innerHTML = `
    <div class="card-header">
      <span>Customer Profile</span>
      <span class="badge badge-olive">#${c.customerId}</span>
    </div>
    <h3 style="color: var(--olive-dark); font-size: 18px; margin-bottom: 2px;">${c.name}</h3>
    <div style="font-size: 13px; color: var(--text-muted); margin-bottom: 14px;">
      📧 ${c.email}<br>
      📞 ${c.phone}<br>
      📍 ${c.address}
    </div>

    <div style="background: var(--olive-light); padding: 14px; border-radius: var(--radius-sm); border: 1px solid var(--border-color); margin-bottom: 16px;">
      <div style="font-size: 12px; font-weight: 700; color: var(--text-muted); text-transform: uppercase;">Lifetime Customer Value</div>
      <div style="font-size: 22px; font-weight: 700; color: var(--olive-primary); margin: 4px 0;">${formatCurrency(totalSpend)}</div>
      <div style="font-size: 12px; color: var(--text-muted);">${txns.length} orders placed</div>
    </div>

    <div style="font-weight: 700; font-size: 13px; margin-bottom: 8px;">Order History</div>
    <div style="max-height: 200px; overflow-y: auto; display: flex; flex-direction: column; gap: 6px;">
      ${txns.length === 0 ? '<p style="color: var(--text-muted); font-size: 12px;">No orders found.</p>' :
        txns.map(t => `
          <div style="display: flex; justify-content: space-between; align-items: center; background: #fafcf7; padding: 8px 10px; border-radius: var(--radius-sm); border: 1px solid var(--border-subtle); font-size: 12.5px;">
            <div>
              <strong>${t.transactionId}</strong>
              <div style="color: var(--text-muted); font-size: 11px;">${t.transactionDate}</div>
            </div>
            <strong style="color: var(--olive-dark);">${formatCurrency(t.totalAmount)}</strong>
          </div>
        `).join('')
      }
    </div>
  `;
}

function openCustomerModal(cust = null) {
  document.getElementById('customerModalTitle').innerText = cust ? `Edit Customer #${cust.customerId}` : 'Add New Customer';
  document.getElementById('cId').value = cust ? cust.customerId : '';
  document.getElementById('cName').value = cust ? cust.name : '';
  document.getElementById('cEmail').value = cust ? cust.email : '';
  document.getElementById('cPhone').value = cust ? cust.phone : '';
  document.getElementById('cAddress').value = cust ? cust.address : '';
  document.getElementById('customerModal').style.display = 'flex';
}

function editCustomer(id) {
  const c = state.customers.find(x => x.customerId === id);
  if (c) openCustomerModal(c);
}

async function handleCustomerSubmit(e) {
  e.preventDefault();
  const data = {
    customerId: document.getElementById('cId').value || '0',
    name: document.getElementById('cName').value.trim(),
    email: document.getElementById('cEmail').value.trim(),
    phone: document.getElementById('cPhone').value.trim(),
    address: document.getElementById('cAddress').value.trim()
  };

  const res = await fetch('/api/customers', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  }).then(r => r.json());

  if (res.success) {
    closeModal('customerModal');
    loadAllData();
  } else {
    alert('Error saving customer: ' + (res.error || 'Unknown error'));
  }
}

async function deleteCustomer(id) {
  if (confirm(`Delete Customer #${id}? This updates customers.csv directly.`)) {
    await fetch(`/api/customers?id=${id}`, { method: 'DELETE' });
    loadAllData();
  }
}

// =========================================================================
// 4. TRANSACTIONS
// =========================================================================
function renderTransactions() {
  const tbody = document.querySelector('#transactionsTable tbody');
  const searchInput = document.getElementById('transactionSearchInput');

  const filter = () => {
    const q = searchInput.value.toLowerCase();
    const filtered = state.transactions.filter(t => {
      return !q || t.transactionId.toLowerCase().includes(q) || t.transactionDate.includes(q) || String(t.customerId).includes(q) || String(t.productId).includes(q);
    });

    tbody.innerHTML = filtered.map(t => {
      const cust = state.customers.find(c => c.customerId === t.customerId);
      const prod = state.products.find(p => p.productId === t.productId);
      return `
        <tr>
          <td><strong>${t.transactionId}</strong></td>
          <td>${t.transactionDate}</td>
          <td>${cust ? cust.name : 'Customer #' + t.customerId}</td>
          <td>${prod ? prod.name : 'Product #' + t.productId}</td>
          <td>${t.quantity} units</td>
          <td><strong>${formatCurrency(t.totalAmount)}</strong></td>
          <td>
            <button class="btn btn-danger btn-sm" onclick="deleteTransaction('${t.transactionId}')">Delete</button>
          </td>
        </tr>
      `;
    }).join('');
  };

  searchInput.oninput = filter;
  filter();
}

function openTransactionModal() {
  const custSel = document.getElementById('tCustomer');
  const prodSel = document.getElementById('tProduct');

  custSel.innerHTML = state.customers.map(c => `<option value="${c.customerId}">${c.name} (#${c.customerId})</option>`).join('');
  prodSel.innerHTML = state.products.map(p => `<option value="${p.productId}" data-price="${p.price}">${p.name} (${formatCurrency(p.price)} | Stock: ${p.stock})</option>`).join('');

  document.getElementById('tDate').value = new Date().toISOString().split('T')[0];
  document.getElementById('tQty').value = 1;
  calculateTransactionTotal();
  document.getElementById('transactionModal').style.display = 'flex';
}

function calculateTransactionTotal() {
  const prodSel = document.getElementById('tProduct');
  const qty = parseInt(document.getElementById('tQty').value || 1);
  const selectedOpt = prodSel.options[prodSel.selectedIndex];
  if (selectedOpt) {
    const price = parseFloat(selectedOpt.dataset.price || 0);
    document.getElementById('tTotal').value = formatCurrency(price * qty);
  }
}

async function handleTransactionSubmit(e) {
  e.preventDefault();
  const prodSel = document.getElementById('tProduct');
  const selectedOpt = prodSel.options[prodSel.selectedIndex];
  const price = parseFloat(selectedOpt ? selectedOpt.dataset.price : 0);
  const qty = parseInt(document.getElementById('tQty').value);

  const data = {
    customerId: document.getElementById('tCustomer').value,
    productId: prodSel.value,
    quantity: qty,
    totalAmount: price * qty,
    transactionDate: document.getElementById('tDate').value
  };

  const res = await fetch('/api/transactions', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  }).then(r => r.json());

  if (res.success) {
    closeModal('transactionModal');
    loadAllData();
  } else {
    alert('Error recording transaction: ' + (res.error || 'Unknown error'));
  }
}

async function deleteTransaction(id) {
  if (confirm(`Delete Transaction ${id}? This updates transactions.csv directly.`)) {
    await fetch(`/api/transactions?id=${id}`, { method: 'DELETE' });
    loadAllData();
  }
}

// =========================================================================
// 5. WAREHOUSES
// =========================================================================
function renderWarehouses() {
  const cardsGrid = document.getElementById('warehouseCardsGrid');
  const tbody = document.querySelector('#warehousesTable tbody');

  cardsGrid.innerHTML = state.warehouses.map(w => `
    <div class="kpi-card">
      <div class="kpi-title">📍 ${w.location}</div>
      <div style="font-weight: 700; color: var(--olive-dark); font-size: 15px; margin: 4px 0;">${w.warehouseName}</div>
      <div style="font-size: 13px; color: var(--olive-primary); font-weight: 600;">Capacity: ${w.capacity.toLocaleString()} units</div>
      <div style="background: #e2e8db; height: 6px; border-radius: 3px; margin-top: 10px; overflow: hidden;">
        <div style="background: var(--olive-primary); width: 45%; height: 100%;"></div>
      </div>
    </div>
  `).join('');

  tbody.innerHTML = state.warehouses.map(w => `
    <tr>
      <td>#${w.warehouseId}</td>
      <td><strong>${w.warehouseName}</strong></td>
      <td>${w.location}</td>
      <td>${w.capacity.toLocaleString()} units</td>
      <td>
        <button class="btn btn-secondary btn-sm" onclick="editWarehouse(${w.warehouseId})">Edit</button>
        <button class="btn btn-danger btn-sm" onclick="deleteWarehouse(${w.warehouseId})">Delete</button>
      </td>
    </tr>
  `).join('');
}

function openWarehouseModal(wh = null) {
  document.getElementById('warehouseModalTitle').innerText = wh ? `Edit Warehouse #${wh.warehouseId}` : 'Add New Warehouse';
  document.getElementById('wId').value = wh ? wh.warehouseId : '';
  document.getElementById('wName').value = wh ? wh.warehouseName : '';
  document.getElementById('wLocation').value = wh ? wh.location : '';
  document.getElementById('wCapacity').value = wh ? wh.capacity : '5000';
  document.getElementById('warehouseModal').style.display = 'flex';
}

function editWarehouse(id) {
  const w = state.warehouses.find(x => x.warehouseId === id);
  if (w) openWarehouseModal(w);
}

async function handleWarehouseSubmit(e) {
  e.preventDefault();
  const data = {
    warehouseId: document.getElementById('wId').value || '0',
    warehouseName: document.getElementById('wName').value.trim(),
    location: document.getElementById('wLocation').value.trim(),
    capacity: document.getElementById('wCapacity').value
  };

  const res = await fetch('/api/warehouses', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  }).then(r => r.json());

  if (res.success) {
    closeModal('warehouseModal');
    loadAllData();
  } else {
    alert('Error saving warehouse: ' + (res.error || 'Unknown error'));
  }
}

async function deleteWarehouse(id) {
  if (confirm(`Delete Warehouse #${id}? This updates warehouses.csv directly.`)) {
    await fetch(`/api/warehouses?id=${id}`, { method: 'DELETE' });
    loadAllData();
  }
}

// =========================================================================
// 6. REVIEWS
// =========================================================================
function renderReviews() {
  const tbody = document.querySelector('#reviewsTable tbody');
  const chipContainer = document.getElementById('reviewKeywordChips');
  const ratingSpread = document.getElementById('reviewRatingSpread');

  // Rating spread cards
  const dist = state.analytics ? state.analytics.ratingDistribution : {};
  ratingSpread.innerHTML = [5, 4, 3, 2, 1].map(stars => `
    <div style="background: var(--olive-light); padding: 8px 14px; border-radius: var(--radius-sm); border: 1px solid var(--border-color); flex: 1; text-align: center;">
      <div style="color: #b58900; font-weight: 700;">${stars} ★</div>
      <div style="font-weight: 700; font-size: 16px; color: var(--olive-dark);">${dist[stars] || 0}</div>
      <div style="font-size: 11px; color: var(--text-muted);">reviews</div>
    </div>
  `).join('');

  // Extract review keywords via client-side scanner based on Aho-Corasick vocabulary
  const kws = ["camera", "performance", "battery", "design", "noise cancellation", "sound", "comfortable", "lighting", "storage", "display", "lightweight", "smooth", "premium", "fast"];
  const kwCounts = {};
  state.reviews.forEach(r => {
    const text = r.reviewText.toLowerCase();
    kws.forEach(k => {
      if (text.includes(k)) kwCounts[k] = (kwCounts[k] || 0) + 1;
    });
  });

  chipContainer.innerHTML = Object.entries(kwCounts).map(([k, count]) => `
    <span class="badge badge-olive" style="font-size: 12px; padding: 5px 10px;">${k.toUpperCase()} (${count})</span>
  `).join('');

  tbody.innerHTML = state.reviews.map(r => {
    const prod = state.products.find(p => p.productId === r.productId);
    const cust = state.customers.find(c => c.customerId === r.customerId);
    return `
      <tr>
        <td>#${r.reviewId}</td>
        <td><strong>${prod ? prod.name : 'Product #' + r.productId}</strong></td>
        <td>${cust ? cust.name : 'Customer #' + r.customerId}</td>
        <td><span style="color: #b58900; font-weight: 700;">${'★'.repeat(r.rating)}${'☆'.repeat(5-r.rating)}</span></td>
        <td>${r.reviewText}</td>
        <td>
          <button class="btn btn-danger btn-sm" onclick="deleteReview(${r.reviewId})">Delete</button>
        </td>
      </tr>
    `;
  }).join('');
}

function openReviewModal() {
  document.getElementById('rProduct').innerHTML = state.products.map(p => `<option value="${p.productId}">${p.name} (#${p.productId})</option>`).join('');
  document.getElementById('rCustomer').innerHTML = state.customers.map(c => `<option value="${c.customerId}">${c.name} (#${c.customerId})</option>`).join('');
  document.getElementById('rText').value = '';
  document.getElementById('reviewModal').style.display = 'flex';
}

async function handleReviewSubmit(e) {
  e.preventDefault();
  const data = {
    productId: document.getElementById('rProduct').value,
    customerId: document.getElementById('rCustomer').value,
    rating: document.getElementById('rRating').value,
    reviewText: document.getElementById('rText').value.trim()
  };

  const res = await fetch('/api/reviews', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  }).then(r => r.json());

  if (res.success) {
    closeModal('reviewModal');
    loadAllData();
  } else {
    alert('Error submitting review: ' + (res.error || 'Unknown error'));
  }
}

async function deleteReview(id) {
  if (confirm(`Delete Review #${id}? This updates reviews.csv directly.`)) {
    await fetch(`/api/reviews?id=${id}`, { method: 'DELETE' });
    loadAllData();
  }
}

// =========================================================================
// 7. DATA EXPLORER
// =========================================================================
function initExplorerTabs() {
  document.querySelectorAll('.explorer-tab').forEach(tab => {
    tab.addEventListener('click', () => {
      document.querySelectorAll('.explorer-tab').forEach(t => {
        t.classList.remove('btn-primary');
        t.classList.add('btn-secondary');
      });
      tab.classList.remove('btn-secondary');
      tab.classList.add('btn-primary');
      state.activeEntity = tab.dataset.entity;
      renderExplorer();
    });
  });
}

function renderExplorer() {
  const container = document.getElementById('explorerTableContainer');
  if (!container) return;

  switch (state.activeEntity) {
    case 'products':
      container.innerHTML = `
        <table>
          <thead><tr><th>ID</th><th>Code</th><th>Name</th><th>Category</th><th>Price</th><th>Stock</th><th>Rating</th></tr></thead>
          <tbody>${state.products.map(p => `<tr><td>#${p.productId}</td><td>${p.productCode}</td><td><strong>${p.name}</strong></td><td>${p.category}</td><td>${formatCurrency(p.price)}</td><td>${p.stock}</td><td>${p.rating} ★</td></tr>`).join('')}</tbody>
        </table>
      `;
      break;
    case 'customers':
      container.innerHTML = `
        <table>
          <thead><tr><th>ID</th><th>Name</th><th>Email</th><th>Phone</th><th>Address</th></tr></thead>
          <tbody>${state.customers.map(c => `<tr><td>#${c.customerId}</td><td><strong>${c.name}</strong></td><td>${c.email}</td><td>${c.phone}</td><td>${c.address}</td></tr>`).join('')}</tbody>
        </table>
      `;
      break;
    case 'transactions':
      container.innerHTML = `
        <table>
          <thead><tr><th>Txn ID</th><th>Date</th><th>Customer ID</th><th>Product ID</th><th>Qty</th><th>Total Amount</th></tr></thead>
          <tbody>${state.transactions.map(t => `<tr><td><strong>${t.transactionId}</strong></td><td>${t.transactionDate}</td><td>#${t.customerId}</td><td>#${t.productId}</td><td>${t.quantity}</td><td>${formatCurrency(t.totalAmount)}</td></tr>`).join('')}</tbody>
        </table>
      `;
      break;
    case 'reviews':
      container.innerHTML = `
        <table>
          <thead><tr><th>ID</th><th>Product ID</th><th>Customer ID</th><th>Rating</th><th>Review Commentary</th></tr></thead>
          <tbody>${state.reviews.map(r => `<tr><td>#${r.reviewId}</td><td>#${r.productId}</td><td>#${r.customerId}</td><td>${r.rating} ★</td><td>${r.reviewText}</td></tr>`).join('')}</tbody>
        </table>
      `;
      break;
    case 'warehouses':
      container.innerHTML = `
        <table>
          <thead><tr><th>ID</th><th>Warehouse Name</th><th>Location</th><th>Storage Capacity</th></tr></thead>
          <tbody>${state.warehouses.map(w => `<tr><td>#${w.warehouseId}</td><td><strong>${w.warehouseName}</strong></td><td>${w.location}</td><td>${w.capacity.toLocaleString()} units</td></tr>`).join('')}</tbody>
        </table>
      `;
      break;
  }
}

// =========================================================================
// 8. ANALYTICS
// =========================================================================
function renderAnalytics() {
  const a = state.analytics;
  if (!a) return;

  const kpis = document.getElementById('analyticsKpis');
  kpis.innerHTML = `
    <div class="kpi-card">
      <div class="kpi-title">Gross Revenue</div>
      <div class="kpi-value">${formatCurrency(a.totalRevenue)}</div>
      <div class="kpi-subtext" style="color: #2e7d32;">From 100% Verified Orders</div>
    </div>
    <div class="kpi-card">
      <div class="kpi-title">Avg Order Value</div>
      <div class="kpi-value">${formatCurrency(a.averageOrderValue)}</div>
      <div class="kpi-subtext">Per Basket Average</div>
    </div>
    <div class="kpi-card">
      <div class="kpi-title">Total Inventory Units</div>
      <div class="kpi-value">${a.totalInventory}</div>
      <div class="kpi-subtext">Across All SKUs</div>
    </div>
    <div class="kpi-card">
      <div class="kpi-title">Network Capacity</div>
      <div class="kpi-value">${a.totalCapacity.toLocaleString()}</div>
      <div class="kpi-subtext">${a.warehouseUtilization.toFixed(1)}% Currently Utilized</div>
    </div>
  `;

  // Render Charts
  const trendCtx = document.getElementById('analyticsTrendChart');
  if (trendCtx) {
    if (charts.aTrend) charts.aTrend.destroy();
    charts.aTrend = new Chart(trendCtx, {
      type: 'line',
      data: {
        labels: Object.keys(a.salesByDate),
        datasets: [{
          label: 'Sales (Rs.)',
          data: Object.values(a.salesByDate),
          borderColor: '#556b2f',
          backgroundColor: 'rgba(85, 107, 47, 0.12)',
          fill: true,
          tension: 0.35,
          borderWidth: 2.5
        }]
      },
      options: { responsive: true, plugins: { legend: { display: false } } }
    });
  }

  const catCtx = document.getElementById('analyticsCategoryChart');
  if (catCtx) {
    if (charts.aCat) charts.aCat.destroy();
    charts.aCat = new Chart(catCtx, {
      type: 'bar',
      data: {
        labels: Object.keys(a.revenueByCategory),
        datasets: [{
          label: 'Revenue (Rs.)',
          data: Object.values(a.revenueByCategory),
          backgroundColor: '#6b8e23',
          borderRadius: 6
        }]
      },
      options: { responsive: true, plugins: { legend: { display: false } } }
    });
  }

  const ratingCtx = document.getElementById('analyticsRatingChart');
  if (ratingCtx) {
    if (charts.aRating) charts.aRating.destroy();
    charts.aRating = new Chart(ratingCtx, {
      type: 'bar',
      data: {
        labels: ['5 Stars', '4 Stars', '3 Stars', '2 Stars', '1 Star'],
        datasets: [{
          label: 'Reviews',
          data: [5, 4, 3, 2, 1].map(s => a.ratingDistribution[s] || 0),
          backgroundColor: '#b58900',
          borderRadius: 6
        }]
      },
      options: { responsive: true, plugins: { legend: { display: false } } }
    });
  }

  const prodDistCtx = document.getElementById('analyticsProductDistChart');
  if (prodDistCtx) {
    if (charts.aProdDist) charts.aProdDist.destroy();
    charts.aProdDist = new Chart(prodDistCtx, {
      type: 'bar',
      data: {
        labels: Object.keys(a.productsByCategory),
        datasets: [{
          label: 'SKU Count',
          data: Object.values(a.productsByCategory),
          backgroundColor: '#556b2f',
          borderRadius: 6
        }]
      },
      options: { responsive: true, plugins: { legend: { display: false } } }
    });
  }
}

// =========================================================================
// 9. ALGORITHMS LAB
// =========================================================================
function initAlgoTabs() {
  document.querySelectorAll('.algo-nav-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.algo-nav-btn').forEach(b => {
        b.classList.remove('btn-primary', 'active');
        b.classList.add('btn-secondary');
      });
      btn.classList.remove('btn-secondary');
      btn.classList.add('btn-primary', 'active');

      document.querySelectorAll('.algo-pane').forEach(p => p.style.display = 'none');
      document.getElementById(`algo-pane-${btn.dataset.algo}`).style.display = 'block';
    });
  });
}

function initBudgetSlider() {
  const slider = document.getElementById('bmBudgetSlider');
  const label = document.getElementById('bmBudgetValue');
  slider.oninput = () => {
    label.innerText = formatCurrency(parseFloat(slider.value));
  };
}

async function runBitmaskDP() {
  const budget = document.getElementById('bmBudgetSlider').value;
  const res = await fetch(`/api/algorithms/bitmask?budget=${budget}`).then(r => r.json());
  
  const box = document.getElementById('bmResultBox');
  box.style.display = 'block';
  document.getElementById('bmTitleResult').innerText = `Optimal Bundle Score: ${res.maxScore} (Total Cost: ${formatCurrency(res.totalPrice)})`;
  document.getElementById('bmExplResult').innerText = res.explanation;
  document.getElementById('bmSelectedItems').innerHTML = res.selectedProducts.map(p => `
    <span class="badge badge-olive" style="font-size: 13px; padding: 6px 12px;">✓ ${p.name} (${formatCurrency(p.price)})</span>
  `).join('');
}

async function runLevenshtein() {
  const s1 = encodeURIComponent(document.getElementById('levS1').value.trim());
  const s2 = encodeURIComponent(document.getElementById('levS2').value.trim());
  const res = await fetch(`/api/algorithms/levenshtein?s1=${s1}&s2=${s2}`).then(r => r.json());

  const box = document.getElementById('levResultBox');
  box.style.display = 'block';
  document.getElementById('levTitleResult').innerText = `Edit Distance: ${res.distance} operations (${res.isClose ? 'CLOSE MATCH ✓' : 'DISTANT'})`;
  document.getElementById('levExplResult').innerText = res.explanation;
}

async function runMatrixChain() {
  const dims = encodeURIComponent(document.getElementById('mcmDims').value.trim());
  const res = await fetch(`/api/algorithms/matrixchain?dims=${dims}`).then(r => r.json());

  const box = document.getElementById('mcmResultBox');
  box.style.display = 'block';
  document.getElementById('mcmTitleResult').innerText = `Minimum Pipeline Cost: ${res.minCost.toLocaleString()}`;
  document.getElementById('mcmExplResult').innerText = res.explanation;
}

async function runAhoCorasick() {
  const text = document.getElementById('acText').value;
  const keywords = document.getElementById('acKeywords').value;
  const res = await fetch('/api/algorithms/ahocorasick', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ text, keywords })
  }).then(r => r.json());

  const box = document.getElementById('acResultBox');
  box.style.display = 'block';
  document.getElementById('acTitleResult').innerText = `Aho-Corasick Matches Found: ${res.matchedKeywords.length}`;
  document.getElementById('acExplResult').innerText = res.explanation;
  document.getElementById('acMatchedChips').innerHTML = res.matchedKeywords.map(k => `
    <span class="badge badge-olive" style="font-size: 12px; padding: 4px 10px;">${k.toUpperCase()}</span>
  `).join('');
}

async function runPatternMatch(algo) {
  const text = encodeURIComponent(document.getElementById('pmText').value.trim());
  const pattern = encodeURIComponent(document.getElementById('pmPattern').value.trim());
  const res = await fetch(`/api/algorithms/${algo}?text=${text}&pattern=${pattern}`).then(r => r.json());

  const box = document.getElementById('pmResultBox');
  box.style.display = 'block';
  document.getElementById('pmTitleResult').innerText = `Algorithm: ${algo.toUpperCase()} Result: ${res.found !== undefined ? (res.found ? 'MATCH FOUND' : 'NOT FOUND') : (res.contains ? 'MATCH FOUND' : 'NOT FOUND')}`;
  document.getElementById('pmExplResult').innerText = res.explanation;
}

async function runSuffixArray() {
  const text = encodeURIComponent(document.getElementById('saText').value.trim());
  const res = await fetch(`/api/algorithms/suffixarray?text=${text}`).then(r => r.json());

  const box = document.getElementById('saResultBox');
  box.style.display = 'block';
  document.getElementById('saExplResult').innerText = res.explanation;
  const tbody = document.querySelector('#saTable tbody');
  tbody.innerHTML = res.entries.map(e => `
    <tr>
      <td>${e.rank}</td>
      <td>Index ${e.pos}</td>
      <td><strong>${e.lcp}</strong></td>
      <td><code>${e.suffix}</code></td>
    </tr>
  `).join('');
}

async function runSuffixQuery(algo) {
  const text = encodeURIComponent(document.getElementById('samText').value.trim());
  const pattern = encodeURIComponent(document.getElementById('samPattern').value.trim());
  const res = await fetch(`/api/algorithms/${algo}?text=${text}&pattern=${pattern}`).then(r => r.json());

  const box = document.getElementById('samResultBox');
  box.style.display = 'block';
  document.getElementById('samTitleResult').innerText = `${algo.toUpperCase()}: ${res.found ? 'SUBSTRING LOCATED ✓' : 'NOT FOUND'}`;
  document.getElementById('samExplResult').innerText = res.explanation;
}

// =========================================================================
// 10. DATA MANAGEMENT
// =========================================================================
async function renderDataStatus() {
  const res = await fetch('/api/data').then(r => r.json());
  const tbody = document.querySelector('#dataStatusTable tbody');
  if (!tbody) return;

  const names = {
    'products.csv': 'Products Catalog',
    'customers.csv': 'Customers Directory',
    'reviews.csv': 'Reviews Ledger',
    'transactions.csv': 'Transactions Ledger',
    'warehouses.csv': 'Warehouses Directory'
  };

  tbody.innerHTML = res.map(f => `
    <tr>
      <td><strong>${names[f.file] || f.file}</strong></td>
      <td><code>${f.file}</code></td>
      <td style="font-size: 12px; color: var(--text-muted);">${f.path}</td>
      <td>${f.size.toLocaleString()} bytes</td>
      <td><span class="badge ${f.exists ? 'badge-olive' : 'badge-rose'}">${f.exists ? '● ONLINE' : '○ MISSING'}</span></td>
    </tr>
  `).join('');
}

async function reloadData() {
  if (confirm('Reload all CSV repositories from disk?')) {
    await fetch('/api/data/reload', { method: 'POST' });
    loadAllData();
    alert('All CSV records reloaded successfully!');
  }
}

async function saveData() {
  await fetch('/api/data/save', { method: 'POST' });
  loadAllData();
  alert('All changes flushed and saved to CSV successfully!');
}

async function backupData() {
  const res = await fetch('/api/data/backup', { method: 'POST' }).then(r => r.json());
  if (res.success) {
    alert('Timestamped backup archive created in:\n' + res.backupPath);
  }
}

// =========================================================================
// GLOBAL SMART SEARCH
// =========================================================================
function initGlobalSearch() {
  const input = document.getElementById('globalSearchInput');
  const dropdown = document.getElementById('searchDropdown');

  let debounceTimer;
  input.addEventListener('input', () => {
    clearTimeout(debounceTimer);
    const q = input.value.trim();
    if (!q) {
      dropdown.style.display = 'none';
      return;
    }

    debounceTimer = setTimeout(async () => {
      const results = await fetch(`/api/search?q=${encodeURIComponent(q)}`).then(r => r.json());
      if (results.length === 0) {
        dropdown.innerHTML = '<div style="padding: 12px; color: var(--text-muted); font-size: 13px;">No matching records found.</div>';
      } else {
        dropdown.innerHTML = results.slice(0, 8).map(r => `
          <div class="search-item" onclick="handleSearchResultClick('${r.type}')">
            <div style="display: flex; justify-content: space-between;">
              <strong>${r.title}</strong>
              <span class="badge badge-olive">${r.type}</span>
            </div>
            <div style="font-size: 12px; color: var(--text-muted);">${r.subtitle} • ${r.detail}</div>
          </div>
        `).join('');
      }
      dropdown.style.display = 'block';
    }, 200);
  });

  document.addEventListener('click', (e) => {
    if (!e.target.closest('.header-search')) {
      dropdown.style.display = 'none';
    }
  });
}

function handleSearchResultClick(type) {
  document.getElementById('searchDropdown').style.display = 'none';
  switch (type.toUpperCase()) {
    case 'PRODUCT': switchView('products'); break;
    case 'CUSTOMER': switchView('customers'); break;
    case 'TRANSACTION': switchView('transactions'); break;
    case 'WAREHOUSE': switchView('warehouses'); break;
    case 'REVIEW': switchView('reviews'); break;
  }
}

// =========================================================================
// UTILITIES
// =========================================================================
function formatCurrency(amt) {
  return 'Rs. ' + Math.round(amt).toLocaleString('en-IN');
}

function closeModal(id) {
  document.getElementById(id).style.display = 'none';
}
