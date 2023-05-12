# Cards

A service to detect and serve Twitter Cards and Open Graph images in a responsible way.

- Aims to avoid sending excessive traffic to origin images.
- Allows detected images to pinned so that their usages don't unexpectedly in the future.
- Provides thumbnails with sensible caching settings.

[Wellynews](https://github.com/tonytw1/wellynews) uses the Cards service to decorate news item URLs with open graph images:

![Example usage](example-usage.jpg)


# End points

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

ie. Given the page `https://wellington.govt.nz/news-and-events/news-and-information/our-wellington/2023/05/sand-shift-autumn-2023`
with the `og:image` `http://wellington.govt.nz/-/media/news-and-events/news-and-information/news/images/2021/11/oriental-bay-02.jpg?mh=1000&amp;mw=1200`

```
curl -v -XPOST localhost:9000/pinned?url=http%3A%2F%2Fwellington.govt.nz%2F-%2Fmedia%2Fnews-and-events%2Fnews-and-information%2Fnews%2Fimages%2F2021%2F11%2Foriental-bay-02.jpg%3Fmh%3D1000%26amp%3Bmw%3D1200%0A
```

```
"ok"
```

```
GET /pinned?url=pinned image url
```

Responses HTTP 200 with the pinned image.



## Thumbnail

Generate a resized thumbnail of a pinned image.
We use imageproxy for resizing. imageproxy is past the pinned image url (above) as it's origin.

```
GET /thumbnail?url=pinned image url
```

Returns a resized copy of the previously pinned image or HTTP 404 if the image url is not pinned.

ie. Given the previous pinned image url `http://wellington.govt.nz/-/media/news-and-events/news-and-information/news/images/2021/11/oriental-bay-02.jpg?mh=1000&amp;mw=1200`

```
curl -v localhost:9000/thumbnail?url=http%3A%2F%2Fwellington.govt.nz%2F-%2Fmedia%2Fnews-and-events%2Fnews-and-information%2Fnews%2Fimages%2F2021%2F11%2Foriental-bay-02.jpg%3Fmh%3D1000%26amp%3Bmw%3D1200%0A
```


## Local development

Use Docker to start a local instance of imageproxy:

```
docker run -p 8080:8080 -it darthsim/imgproxy
```

Review `conf/application.properties`

Start the application:

```
sbt run
```

The service is now running on `http://localhost:9000`