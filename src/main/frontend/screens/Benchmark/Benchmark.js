
/* global $$ */

'use strict';

(async function () {

    const WS = 'services/Benchmark';
    const resultsDiv = $$('result-content');

    function showResult(res) {
        if (res.error) {
            resultsDiv.setValue('<span class="error">Error: ' + res.error + '</span>');
            return;
        }
        
        let html = '';
        for (const [key, value] of Object.entries(res)) {
            html += '<div class="result-row">';
            html += '<span class="result-label">' + key + ':</span>';
            html += '<span class="result-value">' + value + '</span>';
            html += '</div>';
        }
        resultsDiv.setValue(html);
    }

    $$('btn-setup').onclick(async () => {
        const res = await Server.call(WS, 'setupTable');
        showResult(res);
    });

    $$('btn-bulk-insert').onclick(async () => {
        const res = await Server.call(WS, 'bulkInsert', { count: 100 });
        showResult(res);
    });

    $$('btn-bulk-insert-1k').onclick(async () => {
        const res = await Server.call(WS, 'bulkInsert', { count: 1000 });
        showResult(res);
    });

    $$('btn-select-all').onclick(async () => {
        const res = await Server.call(WS, 'selectAll');
        showResult(res);
    });

    $$('btn-count').onclick(async () => {
        const res = await Server.call(WS, 'countRecords');
        showResult(res);
    });

    $$('btn-bulk-update').onclick(async () => {
        const res = await Server.call(WS, 'bulkUpdate');
        showResult(res);
    });

    $$('btn-bulk-delete').onclick(async () => {
        const res = await Server.call(WS, 'bulkDelete');
        showResult(res);
    });

    $$('btn-agg-sum').onclick(async () => {
        const res = await Server.call(WS, 'aggregateSum');
        showResult(res);
    });

    $$('btn-agg-avg').onclick(async () => {
        const res = await Server.call(WS, 'aggregateAvg');
        showResult(res);
    });

    $$('btn-agg-group').onclick(async () => {
        const res = await Server.call(WS, 'aggregateGroupBy');
        showResult(res);
    });

})();
