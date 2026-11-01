# Test building the new openIpoCenter functions
import re

new_code = '''
    function openIpoSubscriptionDetail(s) {
        const panel = showFlow('Detail Pesanan e-IPO');
        
        // Kartu Rincian Pemesanan (Referensi tampilan_ordert_34b2c0.webp)
        const orderCard = node('section', 'card');
        orderCard.style.cssText = 'border-left: 4px solid var(--teal); margin-bottom: 16px; padding: 16px;';
        
        const cardHeader = node('div');
        cardHeader.style.cssText = 'display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 14px; border-bottom: 1px solid var(--border); padding-bottom: 10px;';
        cardHeader.innerHTML = `<div><strong style="font-size:1.15rem; color:#FFF;">${s.ticker}</strong><br><small style="color:var(--text-muted);">${s.companyName || ''}</small></div><span class="m-tag ${s.status === 'ALLOTTED' ? 'pos' : s.status === 'CANCELLED' ? 'neg' : 'neutral'}">${s.statusLabel || s.status}</span>`;
        orderCard.append(cardHeader);

        const grid = node('div');
        grid.style.cssText = 'display: flex; flex-direction: column; gap: 8px; font-size: 0.85rem;';
        
        const pctAllotted = s.orderedLots > 0 && s.allottedLots > 0 ? ` (${((s.allottedLots / s.orderedLots) * 100).toFixed(0)}%)` : '';
        const allottedDisplay = s.status === 'ALLOTTED' ? `${s.allottedLots} lot${pctAllotted}` : (s.status === 'CANCELLED' ? 'Dibatalkan (0 lot)' : 'Menunggu Penjatahan');

        const rows = [
            ['Order ID', s.id || `IPO-${s.ticker}-001`],
            ['Allocation Type', 'Pool (Penjatahan Terpusat)'],
            ['Jumlah Lot Pemesanan', `${s.orderedLots} lot`],
            ['Jumlah Lot Penjatahan', allottedDisplay],
            ['Harga Pelaksanaan', s.price || '-'],
            ['Nilai Penjatahan', s.allotmentValue || (s.status === 'ALLOTTED' ? s.reservedMoney : 'Menunggu Penjatahan')]
        ];

        rows.forEach(([k, v]) => {
            const r = node('div');
            r.style.cssText = 'display: flex; justify-content: space-between; align-items: center; padding: 3px 0;';
            r.innerHTML = `<span style="color:var(--text-muted);">${k}</span><strong style="color:var(--text); text-align:right;">${v}</strong>`;
            grid.append(r);
        });
        orderCard.append(grid);
        panel.append(orderCard);

        // Timeline Tracker 5-Tahap (Referensi tampilan_ordert_34b2c0.webp)
        const timelineCard = node('section', 'card');
        timelineCard.style.cssText = 'margin-bottom: 16px; padding: 16px;';
        timelineCard.append(node('h2', 'card-title', 'Proses & Linimasa e-IPO'));

        const isCancelled = s.status === 'CANCELLED';
        const isAllotted = s.status === 'ALLOTTED';

        const timelineSteps = [
            { title: 'Penawaran Awal (Bookbuilding)', date: s.offeringStartDate || 'Selesai', status: 'done', desc: 'Pengumpulan minat investor' },
            { title: 'Penawaran Umum (Offering)', date: s.offeringEndDate || 'Selesai', status: isCancelled ? 'fail' : 'done', desc: isCancelled ? 'Pemesanan dibatalkan pemodal' : 'Pemesanan terkonfirmasi (Confirmed)' },
            { title: 'Penjatahan Efek (Allotment)', date: s.allotmentDate || 'Selesai', status: isCancelled ? 'fail' : isAllotted ? 'done' : 'active', desc: isCancelled ? 'Tidak berpartisipasi' : isAllotted ? 'Allotted w/ Scale Back (Penjatahan Terpenuhi)' : 'Proses penjatahan bursa' },
            { title: 'Distribusi Saham', date: s.allotmentDate || 'Selesai', status: isAllotted ? 'done' : 'pending', desc: isAllotted ? 'Saham didistribusikan ke portofolio' : 'Menunggu distribusi' },
            { title: 'IPO (Pencatatan Bursa)', date: s.listingDate || 'Listing', status: isAllotted ? 'active' : 'pending', desc: 'Saham resmi melantai di pasar reguler' }
        ];

        const tlList = node('div');
        tlList.style.cssText = 'display: flex; flex-direction: column; gap: 14px; margin-top: 12px; position: relative; padding-left: 24px;';
        
        // Garis vertikal timeline
        const line = node('div');
        line.style.cssText = 'position: absolute; left: 7px; top: 8px; bottom: 8px; width: 2px; background: var(--border); z-index: 1;';
        tlList.append(line);

        timelineSteps.forEach(st => {
            const item = node('div');
            item.style.cssText = 'position: relative; z-index: 2;';
            const dotColor = st.status === 'done' ? 'var(--teal)' : st.status === 'active' ? 'var(--gold)' : st.status === 'fail' ? 'var(--negative)' : 'var(--text-muted)';
            const dotIcon = st.status === 'done' ? '✓' : st.status === 'fail' ? '✕' : '●';
            
            item.innerHTML = `
                <div style="position: absolute; left: -24px; top: 0px; width: 16px; height: 16px; border-radius: 50%; background: #0E1322; border: 2px solid ${dotColor}; color: ${dotColor}; font-size: 0.65rem; display: flex; align-items: center; justify-content: center; font-weight: bold;">${dotIcon}</div>
                <div style="display: flex; justify-content: space-between; align-items: baseline;">
                    <strong style="font-size: 0.85rem; color: ${st.status === 'pending' ? 'var(--text-muted)' : '#FFF'};">${st.title}</strong>
                    <small style="color: var(--text-muted); font-size: 0.72rem;">${st.date}</small>
                </div>
                <p style="font-size: 0.75rem; color: ${st.status === 'done' && st.title.includes('Penjatahan') ? 'var(--border-focus)' : 'var(--text-secondary)'}; margin: 2px 0 0 0;">${st.desc}</p>
            `;
            tlList.append(item);
        });
        timelineCard.append(tlList);
        panel.append(timelineCard);

        // Tombol Aksi
        if (s.canCancel) {
            const cancelBtn = node('button', 'btn-primary', 'Batalkan Pemesanan (Refund Dana)');
            cancelBtn.style.cssText = 'background: rgba(239,68,68,0.18); border: 1px solid var(--negative); color: var(--negative); margin-bottom: 10px; width: 100%; min-height: 48px;';
            cancelBtn.onclick = () => {
                if (confirm(`Batalkan pemesanan IPO ${s.ticker} (${s.orderedLots} lot)? Dana tertahan ${s.reservedMoney} akan langsung cair kembali ke kas pribadi.`)) {
                    cancelBtn.disabled = true;
                    try {
                        const cRes = JSON.parse(bridge.cancelIpoSubscription(s.id));
                        if (cRes.success) {
                            refreshSnapshot();
                            showGameToast('success', 'Pemesanan Dibatalkan', `Dana ${s.reservedMoney} telah dikembalikan ke kas pribadi.`);
                            closeFlow();
                            openIpoCenter();
                        } else {
                            alert(cRes.message || 'Gagal membatalkan');
                            cancelBtn.disabled = false;
                        }
                    } catch(err) {
                        alert(err.message);
                        cancelBtn.disabled = false;
                    }
                }
            };
            panel.append(cancelBtn);
        }

        const backBtn = node('button', 'btn-secondary', 'Kembali ke Daftar e-IPO');
        backBtn.style.cssText = 'width: 100%; min-height: 48px;';
        backBtn.onclick = () => { closeFlow(); openIpoCenter(); };
        panel.append(backBtn);
    }
'''
print("Length of new helper:", len(new_code))
