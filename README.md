# Cards

##  Detect
```
POST /detect
```
Parse the utf8 encoded HTML page past in of the POST body and return a list of the detected card image URLs.

```
curl -v -XPOST -H 'Content-Type: text/html' --data-binary @page-with-og-image-property.html localhost:9000/detect
```

```
[
    {
        "url": "Fully qualified image URL"
    }
]
```


## Pin

After choosing a detected image we can pin a local copy of it.
This lets us be the local origin for resizing and protects us from the source image driftin.

```
POST /pin?url=detected image url
```

Responses with HTTP 200.
