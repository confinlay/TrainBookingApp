## Distributed Ticketing Platform
This repo contains the source code for a distributed ticketing platform   we developed as part of a university module at KU Leuven, namely, "Distributed Systems".

As of the time of writing this short readme, the platform is still hosted on Google App Engine and is available at https://ds-part-2.ew.r.appspot.com. This application allows users to add/remove tickets to their cart, book all tickets in their cart, and view their previous bookings. If any of the ticket bookings fail (e.g. if one of the tickets has been booked by another user while sitting in the cart), then *none* of the tickets will be booked (the entire booking will fail). Any tickets that have been booked in the process will be released again.

User authentication is done through Firebase, and user data is persisted on Firestore. Bookings are confired via a background Google Pub/Sub worker. Some of the trains on the website (and their corresponding ticket objects) are fetched from third party endpoints, where as others are hosted by our own platform, with associated data managed in the Firestore NoSQL database.

Our report is located in the top level directory of this repo (report-cloud.pdf), which answers a few questions about our implementation. 

I have attacked below a few screenshots of our application.

![image](https://github.com/confinlay/TrainBookingApp/assets/106957733/21b081d1-1cf1-4fda-9a2b-617e87c0e95d)
![3ed1a2d8cae2dd540a52d12c6d4a4669](https://github.com/confinlay/TrainBookingApp/assets/106957733/df9f9d0f-65cb-440e-9392-3fa44ee3c4e8)
![7e73f6f1a6b05abbd33846ce6589f383](https://github.com/confinlay/TrainBookingApp/assets/106957733/fb740d93-6bea-473b-9984-4371fe8cc5ac)
![e60ed354c611b415abb9abd65a49c8bd](https://github.com/confinlay/TrainBookingApp/assets/106957733/c1acb00e-cd39-4fdf-9558-f25a9ad645a6)
