var express = require('express');
var http = require('http');
const path = require('path');
const bodyParser = require('body-parser');
const app = express();
const mongo = require('mongodb');

app.use(bodyParser.urlencoded({ extended: true }));
app.use(express.static(path.join(__dirname, 'public')));

app.get('/', (req, res, next) => {
    res.sendFile(path.join(__dirname, 'public', 'magico.html'));
});
app.post('/Search', (req, res, next) => {
    console.log('data: ', req.body.index);
    var index = req.body.index;
    const MongoClient = mongo.MongoClient;
    const url = 'mongodb://localhost:27017';

    MongoClient.connect(url, { useNewUrlParser: true, useUnifiedTopology: true }, (err, client) => {

        if (err) throw err;

        const db = client.db("myDatabase");


        db.collection('Indexers').find({ Word: index }).toArray().then((docs) => {

            res.render(path.join(__dirname, 'public', 'ResultsPage.ejs'),{Documents:docs});

        }).catch((err) => {

            console.log(err);
        });
    });



});

const server = http.createServer(app);
server.listen(3000);
