code = '''
    function openIpoOrderModal(ipo) {
        const panel = showFlow(`Pesan IPO · ${ipo.ticker}`);
        
        const card = node('section', 'card');
        card.style.cssText = 'padding: 16px; margin-bottom: 14px;';
        
        const header = node('div');
        header.style.cssText = 'display: flex; align-items: center; gap: 12px; margin-bottom: 12px; border-bottom: 1px solid var(--border); padding-bottom: 10px;';
        header.innerHTML = `
            <div style="width:44px; height:44px; border-radius:50%; background:#FFF; color:#151515; display:flex; align-items:center; justify-content:center; font-weight:800; font-size:0.95rem; flex-shrink:0;">${ipo.ticker.slice(0, 2)}</div>
            <div>
                <strong style="font-size: 1.1rem; color: #FFF;">${ipo.ticker}</strong>
                <p style="margin: 2px 0 0 0; font-size: 0.8rem; color: var(--text-muted);">${ipo.companyName}</p>
            </div>
        `;
        card.append(header);

        flowText(card, 'Harga Penawaran', `${ipo.offeringPrice} / lembar`);
        flowText(card, 'Ukuran Satuan', `1 lot = ${ipo.lotSize} lembar saham`);
        flowText(card, 'Saham Ditawarkan', `${Number(ipo.sharesOffered).toLocaleString('id-ID')} lembar`);
        flowText(card, 'Jadwal Listing Bursa', ipo.listingDate);
        panel.append(card);

        const formCard = node('section', 'card');
        formCard.style.cssText = 'padding: 16px; margin-bottom: 14px;';
        formCard.append(node('h3', 'card-title', 'Formulir Pemesanan'));

        const lotLabel = node('label', '', 'Jumlah lot yang dipesan');
        lotLabel.style.cssText = 'font-size: 0.85rem; color: var(--text-secondary); margin-bottom: 4px; display: block;';
        
        const lotInput = node('input', 'input-field');
        lotInput.type = 'number';
        lotInput.min = '1';
        lotInput.value = '10';
        lotInput.style.marginBottom = '10px';

        const estBox = node('div');
        estBox.style.cssText = 'background: rgba(0,0,0,0.3); padding: 10px 12px; border-radius: 8px; margin-bottom: 12px; font-size: 0.82rem;';
        
        const rawP = parseCleanPrice(ipo.offeringPriceRaw || ipo.offeringPrice);
        const updateEst = () => {
            const lots = Math.max(1, parseInt(lotInput.value) || 1);
            const total = lots * ipo.lotSize * rawP;
            estBox.innerHTML = `Perkiraan Dana Tertahan: <strong style="color:var(--gold);">${formatMoney(total, ipo.currency)}</strong><br><small style="color:var(--text-muted);">Dana tertahan dilepaskan/refund jika penjatahan tidak 100%.</small>`;
        };
        lotInput.oninput = updateEst;
        updateEst();

        const errText = node('p');
        errText.setAttribute('role', 'alert');
        errText.style.cssText = 'color: var(--negative); font-size: 0.8rem; margin: 4px 0 10px 0;';

        const submitBtn = node('button', 'btn-primary', 'Konfirmasi Pesan Saham IPO');
        submitBtn.style.cssText = 'width: 100%; min-height: 48px;';
        submitBtn.onclick = () => {
            const lots = Number(lotInput.value);
            if (!Number.isFinite(lots) || lots <= 0) {
                errText.textContent = 'Jumlah lot pemesanan minimal 1 lot.';
                return;
            }
            submitBtn.disabled = true;
            submitBtn.textContent = 'Memproses pemesanan...';
            try {
                const reqId = 'REQ-IPO-' + Date.now();
                const res = JSON.parse(bridge.orderIpo(JSON.stringify({ ipoId: ipo.id, lots, requestId: reqId })));
                if (!res.success) throw new Error(res.message);
                refreshSnapshot();
                showGameToast('success', 'Pemesanan Berhasil', `Pemesanan ${lots} lot ${ipo.ticker} berhasil dicatat.`);
                closeFlow();
                openIpoCenter();
            } catch(e) {
                submitBtn.disabled = false;
                submitBtn.textContent = 'Konfirmasi Pesan Saham IPO';
                errText.textContent = 'Pemesanan gagal: ' + e.message;
            }
        };

        formCard.append(lotLabel, lotInput, estBox, errText, submitBtn);
        panel.append(formCard);
    }
'''
print("Verified openIpoOrderModal code:", len(code))
