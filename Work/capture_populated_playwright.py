import time
from playwright.sync_api import sync_playwright

with sync_playwright() as p:
    browser = p.chromium.launch(headless=True)
    context = browser.new_context(
        viewport={'width': 412, 'height': 915},
        device_scale_factor=1
    )
    page = context.new_page()
    page.goto('http://127.0.0.1:18917/')
    
    # Wait for snapshot and data to render
    page.wait_for_timeout(2000)
    
    # Capture Home screen
    page.screenshot(path='Artifacts/Audit-Before-After/01-home-screen-populated.png')
    print("Captured populated home screen!")
    
    # Navigate to Pasar Tab
    page.click('button[data-tab="pasar"]')
    page.wait_for_timeout(1000)
    page.screenshot(path='Artifacts/Audit-Before-After/02-pasar-screen-populated.png')
    print("Captured populated pasar screen!")
    
    # Navigate to Portofolio Tab
    page.click('button[data-tab="portofolio"]')
    page.wait_for_timeout(1000)
    page.screenshot(path='Artifacts/Audit-Before-After/03-portofolio-screen-populated.png')
    print("Captured populated portofolio screen!")

    # Navigate to Tab Lainnya (Riset)
    page.click('button[data-tab="lainnya"]')
    page.wait_for_timeout(1000)
    page.screenshot(path='Artifacts/Audit-Before-After/04-lainnya-screen-populated.png')
    print("Captured populated lainnya screen!")

    browser.close()
