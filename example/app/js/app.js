$(function(){
    $('#btn-english').click(function(){
        location.href = 'index.html?lang=en'
    });
    $('#btn-spanish').click(function(){
        location.href = 'index.html?lang=es'
    });

    var pageName = location.pathname;
    if ( pageName === '/web/index.html' ) {
        $('#home-tab').addClass('active');
        $('.app-main').show();
    } else if ( pageName === '/web/map.html' ) {
        $('#map-tab').addClass('active');
        $('.app-map').show();
    }
});