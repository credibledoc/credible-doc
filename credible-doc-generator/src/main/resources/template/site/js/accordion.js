'use strict';

function show(elementId, sourceElement) {
    $(".article-h2").hide();
    $(".closable").fadeOut(0);
    $("#" + elementId).fadeIn(500);
    $("section nav ul li a").css("font-weight", "");
    $(sourceElement).css("font-weight","Bold");
}