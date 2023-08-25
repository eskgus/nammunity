var indexMain = {
    init: function() {
        $('#btn-close-window').on('click', function() { // confirmEmail
            window.close();
        });

        $('#btn-back').on('click', function() { // 403
            window.history.back();
        });
    },
    redBox: function(field, pre) {
        var box = document.getElementById(field);

        box.style = 'border: 2px solid #ea3636; background-color: #ffc0cb';
        box.addEventListener('input', function() {
            if (pre != box.value) {
                box.style = 'border: 1px solid #205943';
            } else {
                box.style = 'border: 2px solid #ea3636; background-color: #ffc0cb';
            }
        });
    },
    redBoxNBC: function(field, pre) {  // 배경 색 없는 빨간 박스
        var box = document.getElementById(field);

        box.style = 'border: 1px solid #ea3636';
        box.addEventListener('input', function() {
            if (pre != box.value) {
                box.style = 'border: none';
            } else {
                box.style = 'border: 1px solid #ea3636';
            }
        });
    },
    fail: function (response, data, rb) {  // ajax 응답 알림 창 띄우기
        var size = Object.keys(response).length;
        let firstData = 4;

        for (let i = 0; i < size; i++) {
            let error = Object.keys(response)[i];
            let index = Object.keys(data).indexOf(error);
            firstData = firstData > index ? index : firstData;
            rb(error, data[Object.keys(data)[index]]);
        }

        var firstError = Object.keys(data)[firstData];
        alert(response[firstError]);
        document.getElementById(Object.keys(data)[firstData]).focus();
    }
};

indexMain.init();

export { indexMain };