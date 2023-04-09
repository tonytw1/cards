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
