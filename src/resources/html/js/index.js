$("#btnAddTravelogue").click(function() {
	$("#addlogmodal").modal();
});

$("#btnSubmitNewTravelogue").click(function(e) {
	e.preventDefault();

	// submit form

	// try to create a date instance
	if ($("#date").val().length != 0) {
		var date = new Date($("#date").val() + " " + $("#time").val());
		if (isNaN(date.valueOf())) {
			$('#form-group-date,#form-group-time').addClass("has-error");
			return;
		}
		
		$("#visitdate").val(date.valueOf());
	} else {
		$("#visitdate").val("");
	}

	$.ajax({
		url : '/crud/travelogue/',
		type : 'POST',
		data : new FormData($('#add-log-form')[0]),
		processData : false,
		contentType : false
	}).done(function() {
		$("#addlogmodal").modal("hide");
		window.location.reload();
	}).error(function(e) {
		if (e.responseText == "content length") {
			$('#form-group-content').addClass("has-error");
		}
		if (e.responseText == "visitdate") {
			$('#form-group-date,#form-group-time').addClass("has-error");
		}
		console.log(e);
	});
});

// register images for fancybox
$(".expandable").fancybox({
	openEffect : 'none',
	closeEffect : 'none'
});

// update the content of a log
$(".btnSaveLog").click(function() {
	var log = $(this).closest(".travelogue");

	var box = log.find(".log-edit-content").css("display", "none");
	log.find(".log-content").html(box.val()).css("display", "block");
	log.find(".log-btns").css("display", "none");

	$.ajax({
		url : "/crud/travelogue/" + log.data("log-id"),
		type : 'PUT',
		data : {
			content : box.val()
		}
	}).done(function(e) {
		console.log("done updating", e);
		window.location.reload();
	}).error(function(e) {
		console.log("error updating", e);
	});

	box.val();
});

// cancel updating the content of a log
$(".btnCancelLog").click(function() {
	var log = $(this).closest(".travelogue");

	log.find(".log-content").css("display", "block");
	log.find(".log-edit-content").css("display", "none").val("");
	log.find(".log-btns").css("display", "none");
});

// show prompt for adding pictures
$(".btnAddPictures").click(function() {
	var log = $(this).closest(".travelogue");

	// show modal and make the log id the current log
	$("#imageuploadmodal").modal("show");
	$("#logid").val(log.data("log-id"));
});

var locations;

/**
 * Populates the given location selector with the cached values from "locations"
 * 
 * @param select
 *            The select element to add options to
 */
function populateLocationSelect(select) {
	$(select).empty();
	$(select).append($("<option>").text("").val(-1));
	for (var i = 0; i < locations.length; i++) {
		var loc = locations[i];
		$(select).append($("<option>").text(loc.name).val(i));
	}
}

$(".btnSetLocation").click(function() {
	var log = $(this).closest(".travelogue");

	$(log).find(".placemap").removeClass("hidden").addClass("editable");
	$(log).find(".locationEdit").removeClass("hidden");

	// resize is required to make the map show up
	var map = $(log).find(".map").data("map");
	var centerPoint = map.getCenter();

	google.maps.event.trigger(map, "resize");
	map.panTo(centerPoint);

	// update the marker to look draggable
	if (map.marker) {
		map.marker.setDraggable(true);
		map.marker.setCursor("move");
	}

	// read and cache location list, if not already loaded
	if (locations == undefined) {
		$.getJSON("/ajax/locations").success(function(data) {
			locations = data.locations;

			populateLocationSelect(log.find(".inputLocationPreset"));
		});
	} else {
		populateLocationSelect(log.find(".inputLocationPreset"));
	}
});

$(".inputLocationPreset").change(function(e) {
	if ($(this).val() != -1) {
		var loc = locations[$(this).val()];

		var log = $(this).closest(".travelogue");
		var map = $(log).find(".map").data("map");

		var pos = new google.maps.LatLng(loc.latitude, loc.longitude);
		map.panTo(pos);
		map.setMarker(pos);
		log.find(".inputLocationName").val(loc.name);
	}
})

$(".btnSaveLocation").click(
		function() {
			var log = $(this).closest(".travelogue");
			var map = $(log).find(".map");

			var lat = map.data("latitude");
			var lng = map.data("longitude");

			// the id of the location, or -1 if there is none
			var locationId = parseInt(log.find(".inputLocationId").val());

			// whether this user is the owner of the location object
			var isOwner = log.find(".inputLocationOwner").val() == "true";

			var data = {
				name : log.find(".inputLocationName").val(),
				latitude : lat,
				longitude : lng,
				shared : log.find(".inputLocationShared").is(":checked")
			};

			console.log(log.find(".inputLocationId"), log.find(
					".inputLocationId").val(), "Locationid", locationId,
					"isOwner", isOwner, data);

			var options;
			if (locationId == -1 || !isOwner) {
				console.log("Attempting to create new location");
				options = {
					url : "/crud/location/",
					type : 'POST',
					data : data
				};
			} else {
				console.log("Attempting to update existing location");
				options = {
					url : "/crud/location/" + locationId,
					type : 'PUT',
					data : data
				};
			}

			$.ajax(options).done(function(id, a, b) {
				// once the location is created, read the id from the response
				console.log("done creating location", id, a, b);

				// and update the existing travelogue
				var locationid = parseInt(id);

				$.ajax({
					url : "/crud/travelogue/" + log.data("log-id"),
					type : 'PUT',
					data : {
						locationid : locationid
					}
				}).done(function(e) {
					console.log("done updating travelogue location", e);
					window.location.reload();
				}).error(function(e) {
					console.log("error updating travelogue location", e);
				});

				window.location.reload();
			}).error(function(e) {
				console.log("error updating", e);
			});

			$(log).find(".locationEdit").addClass("hidden");
		});

$(".btnRemoveLocation").click(function() {
	var log = $(this).closest(".travelogue");

	var locationId = parseInt(log.find(".inputLocationId").val());

	if (confirm("Are you sure you want to remove the location from this travelogue?")) {
		$.ajax({
			url : "/crud/location/" + locationId,
			type : 'DELETE'
		}).done(function(e) {
			console.log("done removing travelogue location", e);

			$.ajax({
				url : "/crud/travelogue/" + log.data("log-id"),
				type : 'PUT',
				data : {
					locationid : -1
				}
			}).done(function(e) {
				console.log("done updating travelogue location", e);
				window.location.reload();
			}).error(function(e) {
				console.log("error updating travelogue location", e);
			});
		});
	}
});

$("#btnUploadImages").click(function(e) {
	// submit form
	$.ajax({
		url : '/crud/travelogue_image/',
		type : 'POST',
		data : new FormData($('#img-upload-form')[0]),
		processData : false,
		contentType : false
	}).done(function() {
		window.location.reload();
	}).error(function(e) {
		console.log("error adding image", e);
	});
	e.preventDefault();
	$("#imageuploadmodal").modal("hide");
});

$(".btnEditLog").click(function() {
	var log = $(this).closest(".travelogue");

	var content = log.find(".log-content").css("display", "none");
	log.find(".log-edit-content").css("display", "block").val(content.html());
	log.find(".log-btns").css("display", "block");
});

/**
 * gets a url from the base url of the site
 * 
 * @param url
 *            the url that represents the location
 * @returns The absolute url IE: given /test it would return something like
 *          http://localhost:8080/test
 */
function qualifyURL(url) {
	var a = document.createElement('a');
	a.href = url;
	return a.cloneNode(false).href;
}

$(".btnShareLog").click(function(e) {
	e.stopPropagation();
	
	var log = $(this).closest(".travelogue");

	var that = $(this);
	var oldText = that.text();
	that.text("Please wait...");
	
	
	$.ajax({
		url : '/crud/travelogue/' + log.data("log-id"),
		method: 'PUT',
		data: {
			shared: true
		}
	}).done(function () {
		console.log("Done updating preferences");
		prompt("Give this link to your friends:", qualifyURL('/travelogue?id=' + log.data('log-id')));
	}).always(function() {
		that.text(oldText);
	});
	
	console.log("Sharing log", log.data('log-id'));
});

$(".btnRemovePictures").click(
		function() {
			var log = $(this).closest(".travelogue");

			// stop removing if already removing
			if (log.data("removing-imgs") == true) {
				$(this).text("Remove Pictures");
				log.data("removing-imgs", false);

				log.find('img,video').css('opacity', 'inherit');
				log.find('.img-delete').remove();
				return;
			}

			// create remove buttons
			$(this).text("Stop Removing Pictures");
			log.data("removing-imgs", true);

			var e = log.find(".expandable").append(
					'<span class="img-delete">&times;</span>');
			e.find('img,video').css({
				'opacity' : 0.4
			})

			e.find(".img-delete").click(
					function(e) {
						e.preventDefault();

						$(this).closest(".expandable")
								.removeClass("expandable").remove();

						$.ajax(
								{
									url : '/crud/travelogue_image/'
											+ $(this).parent()
													.find('img,video').data(
															'id'),
									type : 'DELETE'
								}).done(function() {
							$(this).parent().remove();
						}).error(function(e) {
							console.log("error", e);
						});
					});
		});

$(".btnDeleteLog").click(function() {
	var log = $(this).closest(".travelogue");

	if (confirm("Are you sure you want to delete this travelogue?")) {
		$.ajax({
			url : "/crud/travelogue/" + log.data("log-id"),
			type : 'DELETE'
		}).done(function(e) {
			console.log("done deleting", e);
			log.remove();
		}).error(function(e) {
			console.log("error updating", e);
		});
	}
});

$(".log-images .logimg-expand").click(function() {
	$(this).css("display", "none");
	$(this).parent().find(".log-hidden").removeClass("log-hidden");
});