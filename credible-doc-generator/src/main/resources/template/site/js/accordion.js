'use strict';

function show(elementId, sourceElement) {
    $(".article-h2").hide();
    $(".closable").fadeOut(0);
    $("#" + elementId).fadeIn(500);
    $(".menu-a").css("font-weight", "normal");
    $(sourceElement).css("font-weight","bold");
}