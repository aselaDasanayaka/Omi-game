<!DOCTYPE HTML>
<html>
<head>
    <style>
        div.position {
            background-color: yellow;
            width:80px;height:100px;padding:10px;border:1px solid #aaaaaa;
            display:inline-block;
        }
    </style>

    <script src="js/knockout.js"></script>
    <script src="js/jquery.js"></script>
</head>
<body >

<script>
    var id;  // To store id
</script>

<button id="btn_start">Join Game</button>
<br>
<h3>Network card game</h3>
<span data-bind="text: message"></span>

<!--cards-->
<br/>
<div data-bind="visible: shouldShowPlayedCards">

    <div class = "position" style = "margin-left: 110px;" >
        <img data-bind="attr: { src: card2 }" >
    </div>

    <br/>

    <div class = "position">
        <img data-bind="attr: { src: card1 }">
    </div>



    <div class = "position" style = "margin-left: 110px;">
        <img data-bind="attr: { src: card3 }">
    </div>

    <br/>

    <!-- Player's Card -->
    <div class = "position" style = "margin-left: 110px;">
        <img data-bind="attr: { src: card4 }">
    </div>
</div>
<br/>
<div data-bind="foreach: cards , visible: shouldShowHand">
    <img data-bind="attr: { src: image }, click: function(data, event) { PlayCard(image)}"/>
</div>
<br/>
<script>

    //*viewmodel* - JavaScript that defines the data and behavior of your UI
    function AppViewModel() {
        var self = this;
        self.cards = ko.observableArray([
            { image: 'cards/0_1.png' },
            { image: 'cards/1_2.png' },
            { image: 'cards/0_3.png' }
        ])
        self.card1 = ko.observable("cards/0_1.png");
        self.card2 = ko.observable("cards/0_1.png");
        self.card3 = ko.observable("cards/0_1.png");
        self.card4 = ko.observable("cards/0_1.png");
        self.shouldShowHand = ko.observable(false);
        self.shouldShowPlayedCards = ko.observable(false);
        self.message = ko.observable("waiting...");
    }

    viewModel = new AppViewModel();
    ko.applyBindings(viewModel);

    function Update(statusJSON)
    {
        var parsed = JSON.parse(statusJSON);
        viewModel.cards(parsed.cards);
        viewModel.card1(parsed.card1);
        viewModel.card2(parsed.card2);
        viewModel.card3(parsed.card3);
        viewModel.card4(parsed.card4);
        viewModel.shouldShowHand(parsed.showHand);
        viewModel.shouldShowPlayedCards(parsed.showCards);
        viewModel.message(parsed.message);
    }

    var interval;

    /*
     * After Buttton click game is start and polling untill all 4 players connect
     * */

    $("#btn_start").click(function () {
        var MSG = {"state":"initial"};
        $.post("omi",MSG,function (resp) {
            console.log('id is : ',resp.id);
            id = resp.id;   //assign ID
            $("#btn_start").prop("disabled",true);
            interval = setInterval(function () {
                polling();
            },1500);
        });
    });

    /*
     * This is the polling function that poll until stop is true
     * */

    function polling(){
        var MSG = {"state":"wating","id":id};
        $.post("omi",MSG,function (resp) {
            Update(JSON.stringify(resp));

        });
    }

    function PlayCard(card){
        var MSG = {"state":"playing","id":id,"card":card};
        $.post("omi",MSG,function (resp) {

        });
    }

</script>

</body>
</html>

