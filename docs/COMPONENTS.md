# components

* cli wallet
* plugin
* sidecar

The Identity Plugin is written in Java and can not validate Credentials with the Tangle itself.
It makes calls to a small simple "sidecar" service (written in Node.js, using identity-wasm).
