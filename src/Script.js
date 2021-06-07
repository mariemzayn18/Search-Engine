var express = require('express');
var http = require('http');
const path = require('path');
const bodyParser = require('body-parser');
const app = express();
const mongo = require('mongodb');
var stemmer = require('porter-stemmer').stemmer
app.use(bodyParser.urlencoded({ extended: true }));
app.use(express.static(path.join(__dirname, 'public')));
var index;
app.set('view engine', 'ejs');
app.get('/', (req, res, next) => {
    const MongoClient = mongo.MongoClient;
    const url = 'mongodb://localhost:27017';
    MongoClient.connect(url, { useNewUrlParser: true, useUnifiedTopology: true }, (err, client) => {

        if (err) throw err;

        const db = client.db("myDatabase");
        db.collection('Suggestion').find({}).toArray().then((docs) => {

            //console.log(docs);
            res.render(path.join(__dirname, 'public', 'magico.ejs'), { Documents: docs });
            //res.sendFile(path.join(__dirname, 'public', 'magico.html'), { Documents: docs });

        }).catch((err) => {

            //            res.render(path.join(__dirname, 'public', 'magico.html'));
            res.render(path.join(__dirname, 'public', 'magico.ejs'), { Documents: docs });
        });

    });

    //    res.sendFile(path.join(__dirname, 'public', 'magico.html'));
});
app.post('/Search', (req, res, next) => {
    var Mypage;
    if (req.body.index != undefined) {
        index = req.body.index;
        Mypage = 1;
    }
    else {
        Mypage = Number(req.body.mypage);

    }
    const MongoClient = mongo.MongoClient;
    const url = 'mongodb://localhost:27017';

    MongoClient.connect(url, { useNewUrlParser: true, useUnifiedTopology: true }, (err, client) => {

        if (err) throw err;

        const db = client.db("myDatabase");
        db.collection('Suggestion').find({ Word: index }).toArray().then((docs) => {
            console.log(docs.length);
            if (docs.length == 0) {
                db.collection('Suggestion').insertOne({ Word: index }, function (err, res) {

                    console.log('inserted');
                    if (err) throw err;
                });

            } else {
                console.log('already in');
            }

        }).catch((err) => {


        });

        index = stemmer(index.toLowerCase());
        db.collection('Indexers').find({ Word: index }).toArray().then((docs) => {
            res.render(path.join(__dirname, 'public', 'ResultsPage.ejs'), { Documents: docs[0], mypage: Mypage });

        }).catch((err) => {

            console.log(err);
        });
    });



});


app.post('/Search/id', (req, res, next) => {
    const MongoClient = mongo.MongoClient;
    const url = 'mongodb://localhost:27017';
    MongoClient.connect(url, { useNewUrlParser: true, useUnifiedTopology: true }, (err, client) => {

        if (err) throw err;

        const db = client.db("myDatabase");
        index = stemmer(index.toLowerCase());
        db.collection('Indexers').find({ Word: index }).toArray().then((docs) => {

            res.render(path.join(__dirname, 'public', 'ResultsPage.ejs'), { Documents: docs[0], mypage: Mypage });

        }).catch((err) => {

            console.log(err);
        });
    });

});



const server = http.createServer(app);
server.listen(3000);
