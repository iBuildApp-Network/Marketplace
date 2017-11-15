window.addEventListener('load', function() {
    // Checking if Web3 has been injected by the browser (Mist/MetaMask/dApp Widget)
    if (typeof web3 !== 'undefined') {
        console.log("Web3 detected!");
        window.web3 = new Web3(web3.currentProvider);
        var contract = web3.eth.contract([{"constant":true,"inputs":[],"name":"multiAsset","outputs":[{"name":"","type":"address"}],"payable":false,"type":"function"},{"constant":true,"inputs":[],"name":"name","outputs":[{"name":"","type":"string"}],"payable":false,"type":"function"},{"constant":false,"inputs":[{"name":"_spender","type":"address"},{"name":"_value","type":"uint256"}],"name":"approve","outputs":[{"name":"","type":"bool"}],"payable":false,"type":"function"},{"constant":false,"inputs":[],"name":"commitUpgrade","outputs":[{"name":"","type":"bool"}],"payable":false,"type":"function"},{"constant":true,"inputs":[],"name":"getLatestVersion","outputs":[{"name":"","type":"address"}],"payable":false,"type":"function"},{"constant":false,"inputs":[{"name":"_from","type":"address"},{"name":"_to","type":"address"},{"name":"_value","type":"uint256"},{"name":"_reference","type":"string"},{"name":"_sender","type":"address"}],"name":"_forwardTransferFromWithReference","outputs":[{"name":"","type":"bool"}],"payable":false,"type":"function"},{"constant":true,"inputs":[],"name":"totalSupply","outputs":[{"name":"","type":"uint256"}],"payable":false,"type":"function"},{"constant":false,"inputs":[{"name":"_from","type":"address"},{"name":"_spender","type":"address"},{"name":"_value","type":"uint256"}],"name":"emitApprove","outputs":[],"payable":false,"type":"function"},{"constant":false,"inputs":[{"name":"_from","type":"address"},{"name":"_to","type":"address"},{"name":"_value","type":"uint256"}],"name":"transferFrom","outputs":[{"name":"","type":"bool"}],"payable":false,"type":"function"},{"constant":false,"inputs":[{"name":"_from","type":"address"},{"name":"_to","type":"address"},{"name":"_value","type":"uint256"}],"name":"emitTransfer","outputs":[],"payable":false,"type":"function"},{"constant":false,"inputs":[{"name":"_value","type":"uint256"}],"name":"recoverTokens","outputs":[{"name":"","type":"bool"}],"payable":false,"type":"function"},{"constant":true,"inputs":[],"name":"decimals","outputs":[{"name":"","type":"uint8"}],"payable":false,"type":"function"},{"constant":true,"inputs":[],"name":"etoken2","outputs":[{"name":"","type":"address"}],"payable":false,"type":"function"},{"constant":true,"inputs":[],"name":"getPendingVersionTimestamp","outputs":[{"name":"","type":"uint256"}],"payable":false,"type":"function"},{"constant":false,"inputs":[],"name":"purgeUpgrade","outputs":[{"name":"","type":"bool"}],"payable":false,"type":"function"},{"constant":false,"inputs":[],"name":"optIn","outputs":[{"name":"","type":"bool"}],"payable":false,"type":"function"},{"constant":false,"inputs":[{"name":"_from","type":"address"},{"name":"_to","type":"address"},{"name":"_value","type":"uint256"},{"name":"_reference","type":"string"}],"name":"transferFromWithReference","outputs":[{"name":"","type":"bool"}],"payable":false,"type":"function"},{"constant":true,"inputs":[{"name":"_owner","type":"address"}],"name":"balanceOf","outputs":[{"name":"","type":"uint256"}],"payable":false,"type":"function"},{"constant":false,"inputs":[{"name":"_icap","type":"bytes32"},{"name":"_value","type":"uint256"}],"name":"transferToICAP","outputs":[{"name":"","type":"bool"}],"payable":false,"type":"function"},{"constant":false,"inputs":[{"name":"_icap","type":"bytes32"},{"name":"_value","type":"uint256"},{"name":"_reference","type":"string"}],"name":"transferToICAPWithReference","outputs":[{"name":"","type":"bool"}],"payable":false,"type":"function"},{"constant":false,"inputs":[{"name":"_spender","type":"address"},{"name":"_value","type":"uint256"},{"name":"_sender","type":"address"}],"name":"_forwardApprove","outputs":[{"name":"","type":"bool"}],"payable":false,"type":"function"},{"constant":true,"inputs":[],"name":"symbol","outputs":[{"name":"","type":"string"}],"payable":false,"type":"function"},{"constant":false,"inputs":[{"name":"_from","type":"address"},{"name":"_icap","type":"bytes32"},{"name":"_value","type":"uint256"},{"name":"_reference","type":"string"},{"name":"_sender","type":"address"}],"name":"_forwardTransferFromToICAPWithReference","outputs":[{"name":"","type":"bool"}],"payable":false,"type":"function"},{"constant":false,"inputs":[{"name":"_from","type":"address"},{"name":"_icap","type":"bytes32"},{"name":"_value","type":"uint256"},{"name":"_reference","type":"string"}],"name":"transferFromToICAPWithReference","outputs":[{"name":"","type":"bool"}],"payable":false,"type":"function"},{"constant":false,"inputs":[{"name":"_from","type":"address"},{"name":"_icap","type":"bytes32"},{"name":"_value","type":"uint256"}],"name":"transferFromToICAP","outputs":[{"name":"","type":"bool"}],"payable":false,"type":"function"},{"constant":true,"inputs":[],"name":"etoken2Symbol","outputs":[{"name":"","type":"bytes32"}],"payable":false,"type":"function"},{"constant":true,"inputs":[],"name":"getPendingVersion","outputs":[{"name":"","type":"address"}],"payable":false,"type":"function"},{"constant":false,"inputs":[{"name":"_to","type":"address"},{"name":"_value","type":"uint256"}],"name":"transfer","outputs":[{"name":"","type":"bool"}],"payable":false,"type":"function"},{"constant":false,"inputs":[{"name":"_to","type":"address"},{"name":"_value","type":"uint256"},{"name":"_reference","type":"string"}],"name":"transferWithReference","outputs":[{"name":"","type":"bool"}],"payable":false,"type":"function"},{"constant":false,"inputs":[{"name":"_etoken2","type":"address"},{"name":"_symbol","type":"string"},{"name":"_name","type":"string"}],"name":"init","outputs":[{"name":"","type":"bool"}],"payable":false,"type":"function"},{"constant":false,"inputs":[{"name":"_newVersion","type":"address"}],"name":"proposeUpgrade","outputs":[{"name":"","type":"bool"}],"payable":false,"type":"function"},{"constant":false,"inputs":[],"name":"optOut","outputs":[{"name":"","type":"bool"}],"payable":false,"type":"function"},{"constant":true,"inputs":[{"name":"_from","type":"address"},{"name":"_spender","type":"address"}],"name":"allowance","outputs":[{"name":"","type":"uint256"}],"payable":false,"type":"function"},{"constant":true,"inputs":[{"name":"_sender","type":"address"}],"name":"getVersionFor","outputs":[{"name":"","type":"address"}],"payable":false,"type":"function"},{"payable":true,"type":"fallback"},{"anonymous":false,"inputs":[{"indexed":false,"name":"newVersion","type":"address"}],"name":"UpgradeProposal","type":"event"},{"anonymous":false,"inputs":[{"indexed":true,"name":"from","type":"address"},{"indexed":true,"name":"to","type":"address"},{"indexed":false,"name":"value","type":"uint256"}],"name":"Transfer","type":"event"},{"anonymous":false,"inputs":[{"indexed":true,"name":"from","type":"address"},{"indexed":true,"name":"spender","type":"address"},{"indexed":false,"name":"value","type":"uint256"}],"name":"Approval","type":"event"}]).at('0xef88808d0Fbb5c0C2C89F6d9B45EDa53c0150993');

        contract.totalSupply(function(error,data){
           $('.mainbalance').html(data['c'][0]);
           console.log(data);
        });

        contract.name(function(error,data){
            $('.name').html(data);
            console.log(data);
        });

        contract.symbol(function(error,data){
            $('.symbol').html(data);
            console.log(data);
        });

		$("#check-my-balance").click(function(){
			contract.balanceOf(web3.eth.defaultAccount,function(error,data){
				$('.balanceOf').text(data);
				console.log(data);
				$("#check-my-balance").hide();
				$("#my-balance").show();
			});
		});
		
		contract.decimals(function(error,data){
            $('.decimals').text(data);
			console.log(data);
        });


        $('#check_form').submit(function(e){
			e.preventDefault();
			var address = $('#check_address').val();
			contract.balanceOf(address, function(error, data){
				$('#check_result').text(data);
			});
			return false;
        });

    } else {
        $("#errorModal").modal("show");
    }
});