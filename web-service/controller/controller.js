const config = require('../connection/connection');
const mysql = require('mysql2');
const pool = mysql.createPool(config);

pool.on('error', (err) => {
    console.log(err)
});

// db migration
(function () {
    pool.getConnection(async function (err, connection)  {

        var isConnectionReady = false;
        while (!isConnectionReady) {
            if(err) {
                console.log("database not ready yet...")
                console.log("sleep for 5 seconds")
                await new Promise(r => setTimeout(r, 5000));
            }

            isConnectionReady = true
        }

        const query = "CREATE TABLE IF NOT EXISTS messages ( id int(11) NOT NULL AUTO_INCREMENT, sender varchar(10) NOT NULL, receiver varchar(10) NOT NULL, message text NOT NULL, read_status tinyint(1) NOT NULL DEFAULT 0, PRIMARY KEY(id) )"

        try {
            connection.query(query, function (err) {
                if(err) throw err
            })

        } catch (error) {
        }

        console.log("success connect to db")
        console.log("success create table")
    })
}())

module.exports = {
    getAllMessages(req, res) {
        const sender = req.query.from

        pool.getConnection(function (err, connection) {
            if (err) throw err;

            const query = "SELECT message FROM messages WHERE sender = ? AND read_status = false"
            connection.query(query, [sender], function(err, result) {
                if (err) throw err;

                if (result.length === 0) {
                    res.send({
                        success: true,
                        message: `there is no messages from ${sender}`
                    })
                    return
                }

                const updateQuery = "UPDATE messages SET read_status = true WHERE sender = ?"
                connection.query(updateQuery, [sender], function (err)     {
                    if (err) throw err;
                })

                res.send({
                    success: true,
                    message: `success get all messages from ${sender}` ,
                    data: result
                })
            })

            connection.release();
        })
    },

    createMessage(req, res){
        const {
            sender,
            receiver,
            message
        } = req.body

        pool.getConnection(function(err, connection) {
            if (err) throw err;

            const query = 'INSERT INTO messages (sender, receiver, message, read_status) VALUES (?, ?, ?, ?)';
            connection.query(query, [sender, receiver, message, false], function(err) {
                if (err) throw err;

                res.send({
                    success: true,
                    message: `success send a message to ${receiver}`,
                })
            })

            connection.release();
        })
    }
}