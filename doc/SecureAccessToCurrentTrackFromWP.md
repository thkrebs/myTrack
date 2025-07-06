In order to securly access the current track from Wordpress the backend supports API tokens.
A user might have multiple API tokens each having an expiration date. This token needs to be send in the HTTP request header X-Api-Token. In order to make this token not visible and to be able to still use leaflet, the following approach cen be used:

Add a custom shortcode to your wordpress theme which acts as a proxy for the GeoJSOn file.

```
function fetch_protected_geojson() {
// Ensure the endpoint is ONLY meant for authenticated users
if (!is_user_logged_in()) {
wp_send_json_error('Unauthorized', 401);
}

    $token = '----your token here --- ; // Replace with your actual token
    $journeyId = isset($_GET['journey']) ? intval($_GET['journey']) : 0;

    if (!$journeyId) {
        wp_send_json_error('Invalid journey ID', 400);
    }

    // Build the API request
    $url = "https://thkrebs.ddns.net:8443/api/v1/journeys/$journeyId/track";
    $response = wp_remote_get($url, [
        'timeout' => 60,
        'headers' => [
            'X-Api-Token' => $token,
        ],
    ]);

    if (is_wp_error($response)) {
        wp_send_json_error('Failed to retrieve data', 500);
    }

    $body = wp_remote_retrieve_body($response);
    $geojson = json_decode($body);

    if (json_last_error() !== JSON_ERROR_NONE) {
        wp_send_json_error('Invalid GeoJSON format received', 500);
    }

    // Send the raw GeoJSON object directly
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode($geojson);
    wp_die(); // Terminate the script execution
}

add_action('wp_ajax_fetch_geojson', 'fetch_protected_geojson');
add_action('wp_ajax_nopriv_fetch_geojson', 'fetch_protected_geojson'); // Allow for logged-in users

```
Using leaflet the geojson containing the current track for your journey can be accessed as follows:

```
[leaflet-geojson src="https://gerne-unterwegs.de/wp-admin/admin-ajax.php?action=fetch_geojson&journey=1"]{name}[/leaflet-geojson]
```