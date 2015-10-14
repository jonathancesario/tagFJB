$(function () {
    /** Color Seed **/
    Colors = {};
    Colors.index = 0;
    Colors.names = {
        aqua: "#00ffff",
        black: "#000000",
        blue: "#0000ff",
        brown: "#a52a2a",
        darkblue: "#00008b",
        darkcyan: "#008b8b",
        darkgreen: "#006400",
        darkkhaki: "#bdb76b",
        darkmagenta: "#8b008b",
        darkolivegreen: "#556b2f",
        darkorange: "#ff8c00",
        darkorchid: "#9932cc",
        darkred: "#8b0000",
        darksalmon: "#e9967a",
        darkviolet: "#9400d3",
        fuchsia: "#ff00ff",
        gold: "#ffd700",
        green: "#008000",
        indigo: "#4b0082",
        khaki: "#f0e68c",
        lightblue: "#add8e6",
        lightgreen: "#90ee90",
        lightpink: "#ffb6c1",
        lime: "#00ff00",
        magenta: "#ff00ff",
        maroon: "#800000",
        navy: "#000080",
        olive: "#808000",
        orange: "#ffa500",
        pink: "#ffc0cb",
        purple: "#800080",
        red: "#ff0000",
        yellow: "#ffff00"
    };
    Colors.gray = {
        name: "gray",
        rgb: "#7e7e7e",
        rgba: "rgba(" + (8289918 >> 16) + "," + (8289918 >> 8) + "," + (8289918 & 255) + "," + (0.1) + ")"
    };
    Colors.random = function () {
        var result = Object.keys(Colors.names)[(Colors.index) % (Object.keys(Colors.names).length)];
        var num = parseInt(this.names[result].slice(1), 16);
        Colors.index++;
        var gba = [num >> 16 & 255, num >> 8 & 255, num & 255, 0.1];

        return {
            name: result,
            rgb: this.names[result],
            rgba: "rgba(" + gba[0] + "," + gba[1] + "," + gba[2] + "," + gba[3] + ")"
        };
    };

    var MAX_SIZE = 50;

    /** add color property to line object **/
    var seed = function (data) {
        var color = Colors.random();
        return {
            label: data.label,
            fillColor: color.rgba,
            strokeColor: color.rgb,
            pointColor: color.rgb,
            pointStrokeColor: "#fff",
            pointHighlightFill: "#fff",
            pointHighlightStroke: color.rgb,
            data: data.data
        };
    };

    /** add special color (gray) property to line object for past top data **/
    var specialSeed = function (data) {
        return {
            label: data.label,
            fillColor: Colors.gray.rgba,
            strokeColor: Colors.gray.rgb,
            pointColor: Colors.gray.rgb,
            pointStrokeColor: "#fff",
            pointHighlightFill: "#fff",
            pointHighlightStroke: Colors.gray.rgb,
            data: data.data
        };
    };

    /** fetch original data with additional past top data result */
    var lineChartDataWithPastResult = function (data) {
        datalist = [];
        labels   = [];

        var l = 0, r = 0, ll = data.datasets_win.length, rr = data.datasets_lose.length;

        /** merge two sorted datalist */
        while (l < ll && r < rr && datalist.length <= MAX_SIZE) {
            var sl = data.datasets_win[l].data[data.datasets_win[l].data.length-1];
            var sr = data.datasets_lose[r].data[data.datasets_lose[r].data.length-1];

            if (sl >= sr) datalist.push(seed(data.datasets_win[l++]));
            else datalist.push(specialSeed(data.datasets_lose[r++]))
        }
        while (l < ll && datalist.length <= MAX_SIZE) datalist.push(seed(data.datasets_win[l++]));
        while (r < rr && datalist.length <= MAX_SIZE) datalist.push(specialSeed(data.datasets_lose[r++]));


        return {
            labels: data.labels,
            datasets: datalist
        }
    };

    /** display line chart data using specific tag's name */
    var filterLineChartData = function (data,tag) {
        datalist = [];
        for (i in data.datasets) {
            if (data.datasets[i].label === tag) {
                datalist.push(data.datasets[i]);
            }
        }

        return {
            labels: data.labels,
            datasets: datalist
        }
    };


    /** show one line (specific tag name) chart */
    $("#result-table").on("click",'.btn-visualize-detail',function(e) {
        e.preventDefault();

        var tag = $(this).attr('data-id');

        var ctx = $('#canvas')[0].getContext("2d");

        if (typeof $graph != 'undefined') {
            $graph.destroy();
        }

        $data = filterLineChartData(line_data,tag);

        $('#canvas-modal').modal('hide');

        $graph = new Chart(ctx).Line($data, {
            responsive: true,
            multiTooltipTemplate: "<%= datasetLabel %> - <%= value %>"
        });
    });

    /** show one line (specific tag name) chart */
    $("#btn-visualize-tag").click(function(e) {
        e.preventDefault();

        $button = $(this);

        /* disable button */
        $button.addClass('disabled');
        $button.prop('disabled', true);
        $button.html('Please Wait...');

        var tag_name = $("#tag_search").val();

        $.get('/results/tags/' + tag_name, function (resp) {

            var res = JSON.parse(resp);

            if (res.success) {

                var ctx = $('#canvas')[0].getContext("2d");
                if (typeof $graph != 'undefined') {
                    $graph.destroy();
                }

                $graph = new Chart(ctx).Line(lineChartDataWithPastResult(res.data), {
                    responsive: true,
                    multiTooltipTemplate: "<%= datasetLabel %> - <%= value %>"
                });

                $('#canvas-modal').modal('hide');
            }
            else {
                alert(res.message);
            }

            $button.removeClass('disabled');
            $button.prop('disabled', false);
            $button.html('Search');
        });
    });

    /** show chart based on file name */
    $("#visualization-table").on("click",'.btn-visualize',function(e) {
        e.preventDefault();

        $button = $(this);

        /* disable button */
        $button.addClass('disabled');
        $button.prop('disabled', true);
        $button.html('Please Wait...');

        var file_name = $(this).attr('data-id');

        $.get('/results/' + file_name, function (resp) {

            var res = JSON.parse(resp);

            if (res.success) {
                $('#canvas').removeClass("hidden");
                $('#canvas-btn-view').attr('data-id',file_name);
                $('#canvas-btn-reset').attr('data-id',file_name);
                $('#canvas-btn-view').removeClass("hidden");
                $('#canvas-btn-reset').removeClass("hidden");

                var ctx = $('#canvas')[0].getContext("2d");
                if (typeof $graph != 'undefined') {
                    $graph.destroy();
                }

                line_data = lineChartDataWithPastResult(res.data);

                $('html, body').animate({
                    scrollTop: $("#canvas").offset().top
                }, 500);

                $graph = new Chart(ctx).Line(line_data, {
                    responsive: true,
                    multiTooltipTemplate: "<%= datasetLabel %> - <%= value %>"
                });
            }
            else {
                alert(res.message);
            }

            $button.removeClass('disabled');
            $button.prop('disabled', false);
            $button.html('Visualize');
        });
    });

    /** initialization method */
    $('#btn-init').click(function (e) {
        var date = $('#init_date').val();
        var days = $('#init_days').val();
        e.preventDefault();

        $button = $(this);
        /* disable button */

        $button.addClass('disabled');
        $button.prop('disabled', true);
        $button.html('Please Wait...');

        $.post('/results/' + date + '/' + days, function (resp) {
            var res = JSON.parse(resp);

            if (res.success) {
                alert("Done");
                $button.html('Initialized');
            } else {
                $button.html('Initialize');
                alert(res.message);
                $button.removeClass('disabled');
                $button.prop('disabled', false);
            }
        });
    });

    /** get new day method */
    $('#btn-predict').click(function (e) {
        e.preventDefault();

        $button = $(this);

        /* disable button */
        $button.addClass('disabled');
        $button.prop('disabled', true);
        $button.html('Please Wait...');

        $.post('/results', function (resp) {
            var res = JSON.parse(resp);
            if (res.success) {

                $('#visualization-table').append(
                    '<tr id="visualize-row-'+res.all+'">'+
                        '<td> New! </td>'+
                        '<td>'+res.all+'</td>'+
                        '<td><a data-id="'+res.all+'"class="btn btn-xs btn-primary btn-visualize" href="#canvas">Visualize</a></td>'+
                    '</tr>' +

                    '<tr id="visualize-row-'+res.top+'">'+
                        '<td> New! </td>'+
                        '<td>'+res.top+'</td>'+
                        '<td><a data-id="'+res.top+'"class="btn btn-xs btn-primary btn-visualize" href="#canvas">Visualize</a></td>'+
                    '</tr>'
                );

                alert("Done");
            } else {
                alert(res.message);
            }

            $button.removeClass('disabled');
            $button.prop('disabled', false);
            $button.html('Get New Day');
        });
    });

    /** delete all saved results */
    $('#btn-delete').click(function (e) {
        e.preventDefault();

        $button = $(this);

        if(confirm('Are you sure to delete all saved results?')){
            /* disable button */
            $button.addClass('disabled');
            $button.prop('disabled', true);
            $button.html('Please Wait...');

            $.ajax({
                url    : "/results",
                method : 'DELETE',
                success : function(resp){
                    $button.removeClass('disabled');
                    $button.prop('disabled', false);
                    $button.html('Delete All');

                    var res = JSON.parse(resp);
                    if(res.success){
                        $('#visualize-table tr').remove();
                    }else{
                        alert(res.message);
                    }

                }
            });
        }
    });

    /** reset chart */
    $('#canvas-btn-reset').click(function(e) {
        e.preventDefault();
        var ctx = $('#canvas')[0].getContext("2d");
        if (typeof $graph != 'undefined') {
            $graph.destroy();
        }
        $data = line_data;

        $graph = new Chart(ctx).Line($data, {
            responsive: true,
            multiTooltipTemplate: "<%= datasetLabel %> - <%= value %>"
        });
    });

    /** view detailed result table */
    $('#canvas-btn-view').click(function(e){
        e.preventDefault();
        var file_name = $(this).attr('data-id');

        $.get('/results/' + file_name + '/details', function(resp){
            var res = JSON.parse(resp);
            $("#result-table tr").remove();

            if(res.success) {
                var results = JSON.parse(res.data);

                for(var result in results) {
                    $('#result-table').append(
                        '<tr id="'+ results[result].name +'">'+
                            '<td>'+ (parseInt(result)+1) +'</td>'+
                            '<td>'+ results[result].name +'</td>'+
                            '<td>'+ results[result].forum +'</td>'+
                            '<td>'+ results[result].dataPresent.score +'</td>'+
                            '<td>'+ results[result].dataPresent.prob +'</td>'+
                            '<td>'+ results[result].dataPresent.maxProb +'</td>'+
                            '<td> <a data-id="'+ results[result].name +'" class="btn btn-primary btn-sm btn-visualize-detail" href="#">View</a></td>'+
                        '</tr>'
                    );
                }
            }
            $('#canvas-modal').modal('show');
        });
    });
});