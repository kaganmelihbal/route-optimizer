document.addEventListener("DOMContentLoaded", () => {
    // Listen for button click event when the page is loaded
    const calcBtn = document.getElementById('calcBtn');
    if (calcBtn) {
        calcBtn.addEventListener('click', calculate);
    }
});

async function calculate() {
    const from = document.getElementById('from').value.trim();
    const via = document.getElementById('via').value.trim();
    const to = document.getElementById('to').value.trim();
    const consumption = document.getElementById('consumption').value || 0;
    const fuelPrice = document.getElementById('fuelPrice').value || 0;
    const btn = document.getElementById('calcBtn');
    const resultBox = document.getElementById('result');

    if (!from || !to) {
        showError("Please enter both origin and destination cities.");
        return;
    }

    // Show loading state
    btn.classList.add('loading');
    btn.disabled = true;
    resultBox.classList.remove('show');
    
    setTimeout(() => {
        if (!resultBox.classList.contains('show')) {
            resultBox.style.display = 'none';
        }
    }, 500);

    try {
        let url = `/api/route/calculate?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}&consumption=${consumption}&fuelPrice=${fuelPrice}`;
        if (via) {
            url += `&via=${encodeURIComponent(via)}`;
        }

        const res = await fetch(url);

        if (!res.ok) {
            throw new Error("An error occurred while calculating the route. Please check the city names.");
        }

        const data = await res.json();

        // Format the route itinerary
        const itineraryContainer = document.getElementById('itinerary-container');
        if (data.itinerary && data.itinerary.length > 0) {
            let pathHtml = '';
            data.itinerary.forEach((city, index) => {
                const cityName = (typeof city === 'string') ? city : (city?.name ?? '');
                pathHtml += `<span>${cityName}</span>`;
                if (index < data.itinerary.length - 1) {
                    pathHtml += `<i class="fa-solid fa-arrow-right-long path-icon"></i>`;
                }
            });
            itineraryContainer.innerHTML = pathHtml;
        } else {
            itineraryContainer.innerHTML = '<span>Route not found.</span>';
        }

        // Draw the route on the map (window.drawRoute function in map.js)
        if (typeof window.drawRoute === 'function' && Array.isArray(data.itinerary)) {
            window.drawRoute(data.itinerary);
        }

        // Update distance text
        document.getElementById('dist').innerText = data.totalDistance.toLocaleString('en-US');

        // Format fuel cost currency
        let fuelVal = data.fuelCost;
        if (typeof fuelVal === 'number') {
            fuelVal = fuelVal.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
        } else if (typeof fuelVal === 'string' && fuelVal.includes('TL')) {
            fuelVal = fuelVal.replace('TL', '').trim();
        }
        document.getElementById('fuelResult').innerText = fuelVal;

        // Display results
        resultBox.style.display = 'block';
        void resultBox.offsetWidth; // Trigger reflow for animation
        resultBox.classList.add('show');

    } catch (error) {
        showError(error.message);
        console.error(error);
    } finally {
        // Remove loading state
        btn.classList.remove('loading');
        btn.disabled = false;
    }
}

function showError(msg) {
    document.getElementById('toastMessage').innerText = msg;
    const toastElement = document.getElementById('errorToast');
    const toast = new bootstrap.Toast(toastElement);
    toast.show();
}