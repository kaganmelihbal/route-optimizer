// Initialize Leaflet map - Centered on Turkey
const map = L.map('map').setView([39.0, 35.0], 6);

// OpenStreetMap layer
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 18,
    attribution: '&copy; OpenStreetMap'
}).addTo(map);

// GLOBAL VARIABLES
let activeRouteLayer = null;
let cityLookup = {}; // Stores coordinates for all cities (accessible via ID and Name)

// Extracts coordinates from city data
function getCoords(city) {
    if (!city) return null;

    // 1. Check if coordinates exist directly in the object (provided by backend)
    let lat = city.lat ?? city.latitude;
    let lng = city.lng ?? city.longitude;

    // 2. If not, try to find them in the cached city database (cityLookup)
    if ((lat === undefined || lng === undefined) && Object.keys(cityLookup).length > 0) {
        // Search by ID
        let found = cityLookup[city.id];
        
        // Search by Name (Case-insensitive)
        if (!found && city.name) {
            const searchName = city.name.toLowerCase().trim();
            found = Object.values(cityLookup).find(c => c.name.toLowerCase().trim() === searchName);
        }
        
        // If found, extract the coordinates
        if (found) {
            lat = found.lat ?? found.latitude;
            lng = found.lng ?? found.longitude;
            // console.log(`Coordinates resolved for: ${city.name || city.id}`); // Debugging
        }
    }

    if (typeof lat === 'number' && typeof lng === 'number') {
        return { lat, lng };
    }
    return null;
}

// Route drawing function
window.drawRoute = async function(itinerary) {
    console.log("Route drawing triggered. Processing data...");

    if (!Array.isArray(itinerary) || itinerary.length < 2) {
        console.warn("Insufficient route data.");
        return;
    }

    // Prepare coordinates (utilizes smart coordinate matching)
    const coords = itinerary.map(getCoords).filter(Boolean);

    if (coords.length < 2) {
        console.error("ERROR: City coordinates not found!");
        return;
    }

    // Clean up existing route layer
    if (activeRouteLayer) {
        map.removeLayer(activeRouteLayer);
        activeRouteLayer = null;
    }

    // OSRM API Request (Format: Lng, Lat)
    const osrmWaypoints = coords.map(p => `${p.lng},${p.lat}`).join(';');
    const url = `https://router.project-osrm.org/route/v1/driving/${osrmWaypoints}?geometries=geojson&overview=full`;

    try {
        const response = await fetch(url);
        const data = await response.json();

        if (data.code === 'Ok' && data.routes && data.routes[0]) {
            // SUCCESS: Draw OSRM GeoJSON Route
            activeRouteLayer = L.geoJSON(data.routes[0].geometry, {
                style: {
                    color: '#0074D9', // Bright Blue
                    weight: 6,
                    opacity: 0.85,
                    lineJoin: 'round'
                }
            }).addTo(map);

            // Fit map view to the route
            map.fitBounds(activeRouteLayer.getBounds(), { padding: [50, 50] });
            console.log("OSRM route drawn successfully.");
        } else {
            throw new Error("OSRM could not find a valid route");
        }
    } catch (error) {
        console.warn("OSRM failed, falling back to straight-line polyline:", error);
        
        // Fallback: Simple Straight-Line Polyline
        const simplePoints = coords.map(p => [p.lat, p.lng]);
        activeRouteLayer = L.polyline(simplePoints, {
            color: '#EF4444', // Red
            weight: 4,
            dashArray: '10, 10',
            opacity: 0.8
        }).addTo(map);
        
        map.fitBounds(activeRouteLayer.getBounds(), { padding: [30, 30] });
    }
};

// --- DATA LOADING ---
// Load cities.json and cache it into cityLookup
fetch('/cities.json')
    .then(res => res.json())
    .then(cities => {
        if (!Array.isArray(cities)) return;

        // 1. Cache cities for the drawRoute function
        cities.forEach(city => {
            cityLookup[city.id] = city; // Store with ID as primary key
        });

        console.log(`Map data loaded: ${cities.length} cities cached.`);

        // 2. Initialize Markers and default network connections
        const drawnConnections = new Set();

        cities.forEach(city => {
            const coords = getCoords(city);
            if (!coords) return;

            // Connections (Thin gray lines representing available paths)
            if (city.connections) {
                city.connections.forEach(conn => {
                    const target = cityLookup[conn.targetId];
                    const tCoords = getCoords(target);
                    
                    if (tCoords) {
                        const key = [city.id, conn.targetId].sort().join('-');
                        if (!drawnConnections.has(key)) {
                            drawnConnections.add(key);
                            L.polyline([[coords.lat, coords.lng], [tCoords.lat, tCoords.lng]], {
                                color: '#94A3B8', // Muted gray
                                weight: 1,
                                opacity: 0.4
                            }).addTo(map);
                        }
                    }
                });
            }

            // City Marker
            L.marker([coords.lat, coords.lng])
                .addTo(map)
                .bindPopup(`<b>${city.name}</b>`);
        });
    })
    .catch(err => console.error("Error loading cities.json:", err));