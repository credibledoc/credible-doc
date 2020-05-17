'use strict';

$(function () {
    $("#hamburger").click(
        function () {
            const nav = $("nav");
            if (nav.is(":visible")) {
                nav.fadeOut(300);
            } else {
                nav.fadeIn(500);
            }
        }
    );
});

// Get the input field
const input = document.getElementById("er_search_input_dummy");

// Execute a function when the user releases a key on the keyboard
input.addEventListener("keyup", function(event) {
    let code;

    if (event.key !== undefined) {
        code = event.key;
    } else if (event.keyIdentifier !== undefined) {
        code = event.keyIdentifier;
    } else if (event.keyCode !== undefined) {
        code = event.keyCode;
    }
    
    // Number 13 is the "Enter" key on the keyboard
    if (code === 13 || "Enter" === code) {
        // Cancel the default action, if needed
        event.preventDefault();
        // Trigger the button element with a click
        document.getElementById("er_search_button_dummy").click();
    }
});

function search() {
    const domContainer = document.getElementById("er_search_input_dummy");
    let searchText = domContainer.value;
    let query = searchText.replace(/\s/g, "+");
    location.href = 'https://www.google.com/search?q=credibledoc+' + query;
}