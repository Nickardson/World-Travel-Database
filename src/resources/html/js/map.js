// this script, when included in a page, alongside the Google maps API will:
// * search for elements of the class "placemap", and turn them into maps.
// * the initial position of the map is determined by the data attributes on the placemap "latitude" and "longitude". An optional "zoom" is allowed.
// * when a marker is placed or moved, the data attributes "latitude" and "longitude" are updated on the placemap

function initMaps() {
	function createMap(parent, options) {
		var map = $("<div>").addClass("map").appendTo(parent);
		if (options.mapTypeId === undefined) {
			options.mapTypeId = google.maps.MapTypeId.HYBRID;			
		}
		
		var gmap = new google.maps.Map(map[0], options);
		
		google.maps.event.addListener(gmap, 'click', function(e) {
			if (map.parent().hasClass("editable")) {
				placeMarker(e.latLng, gmap, map);
				
				// TODO: bit of a hack to update when dragged or new
				$(map).closest(".travelogue").find(".btnSaveLocation").click();				
			}
		});
		
		return {
			element: map,
			map: gmap
		};
	}

	function placeMarker(position, gmap, element) {
		if (gmap.marker != null) {
			gmap.marker.setMap(null);
			gmap.marker = null;
		}
		// TODO: make the marker draggable when editable changes
		var editable = element.parent().hasClass("editable");
		gmap.marker = new google.maps.Marker({
			draggable: editable,
			position : position,
			map : gmap,
			cursor: editable ? "move" : "grab"
		});
		
		google.maps.event.addListener(gmap.marker, 'dragend', function(e) {
			console.log('Dragged to', "Latitude: " + gmap.marker.getPosition().lat() + ", Longitude: " + gmap.marker.getPosition().lng())
			
			$(element).data("latitude", gmap.marker.getPosition().lat());
			$(element).data("longitude", gmap.marker.getPosition().lng());
			
			// TODO: bit of a hack to update when dragged or new
			$(element).closest(".travelogue").find(".btnSaveLocation").click();
		});
		
		// map.panTo(position);
		
		$(element).data("latitude", position.lat());
		$(element).data("longitude", position.lng());
		
		window.postMessage(position.lat() + "," + position.lng(), "*");
		
		console.log('Placed new', "Latitude: " + position.lat() + ", Longitude: " + position.lng())
	}
	
	$(".placemap").each(function () {
		var lat = $(this).data("latitude");
		var lng = $(this).data("longitude");
		
		// point is where the data attributes set it, or around Orlando, FL
		var point, shouldCreateMarker = false;
		if (lat !== undefined && lng !== undefined) {
			point = new google.maps.LatLng(lat, lng);
			shouldCreateMarker = true;
		} else {
			point = new google.maps.LatLng(28.51, -81.36)
		}
		
		var result = createMap($(this), {
			center: point,
			zoom : $(this).data("zoom") || 5
		});
		
		// place point if it exists
		if (shouldCreateMarker) {
			placeMarker(point, result.map, result.element);
		}
		
		$(result.element).data("map", result.map);
	})
}