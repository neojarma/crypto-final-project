const router = require('express').Router();
const {
    controller
} = require('../controller');

router.get('/message', controller.getAllMessages);
router.post('/message', controller.createMessage);

module.exports = router;