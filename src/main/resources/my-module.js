var uuid = require('uuid');
var a = require('stsTest');

module.exports = {
    someUuid: uuid.v4(),
    Foo: new a.Foo()
};