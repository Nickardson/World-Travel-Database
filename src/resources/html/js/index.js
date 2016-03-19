$("#btnAddTravelogue").click(function() {
	$("#addlogmodal").modal();
});

function pad(a) {
	if (a < 10) {
		return "0" + a;
	} else {
		return a;
	}
}

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

		// var str = date.getUTCFullYear() + "-" +
		// pad(1 + date.getUTCMonth()) + "-" +
		// pad(date.getDate()) + " " +
		// pad(date.getHours()) + ":" + pad(date.getMinutes()) + ":" +
		// pad(date.getSeconds());

		$("#visitdate").val(date.valueOf());
		// console.log(date, date.valueOf());
		// console.log(str);
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

$(".expandable").fancybox({
	openEffect : 'none',
	closeEffect : 'none'
});

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

$(".btnCancelLog").click(function() {
	var log = $(this).closest(".travelogue");

	log.find(".log-content").css("display", "block");
	log.find(".log-edit-content").css("display", "none").val("");
	log.find(".log-btns").css("display", "none");
});

$(".btnAddPictures").click(function() {
	var log = $(this).closest(".travelogue");

	// show modal and make the log id the current log
	$("#imageuploadmodal").modal("show");
	$("#logid").val(log.data("log-id"));
});

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
	map.marker.setDraggable(true);
	map.marker.setCursor("move");
});

$(".btnSaveLocation").click(function() {
	var log = $(this).closest(".travelogue");
	var map = $(log).find(".map");

	var lat = map.data("latitude");
	var lng = map.data("longitude");

	// TODO: create a 'Location'
	// TODO: creating a new location every time is temporary, there should be a
	// whole editing and choosing thing for locations
	$.ajax({
		url : "/crud/location/",
		type : 'POST',
		data : {
			name : 'Map', // TODO: allow new name
			latitude : lat,
			longitude : lng,
			shared : false
		// TODO: allow sharing
		}
	}).done(function(id, a, b) {
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

	// $.ajax({
	// url: "/crud/travelogue/" + log.data("log-id"),
	// type: 'PUT',
	// data: {
	// content: box.val()
	// }
	// }).done(function (e) {
	// console.log("done updating",e);
	// window.location.reload();
	// }).error(function (e) {
	// console.log("error updating",e);
	// });

	$(log).find(".locationEdit").addClass("hidden");
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

$(".btnShareLog").click(
		function() {
			var log = $(this).closest(".travelogue");

			console.log("Sharing log", log.data('log-id'));
			prompt("Give this link to your friends:",
					qualifyURL('/travelogue?id=' + log.data('log-id')));
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