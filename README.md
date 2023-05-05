# Cards

##  Detect

Detect Twitter Cards and Open Graph images on an HTML page.

```
POST /detect
```
Parse the utf8 encoded HTML page past in on the POST body and return a list of the detected card image URLs.

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
This lets us be the local origin for resizing and protects us if the source image drifts.

```
POST /pinned?url=detected image url
```

Responses with HTTP 200.

```
GET /pinned?url=pinned image url
```

Responses HTTP 200 with the pinned image.



## Thumbnail

Generate a resized thumbnail of a pinned image.

```
GET /thumbnail?url=pinned image url
```

We use imageproxy for resizing. imageproxy is past the pinned image url (above) as it's origin.


