## Distributed Ticketing Platform
This repo contains the source code for a distributed ticketing platform   we developed as part of a university module at KU Leuven, namely, "Distributed Systems".

As of the time of writing this short readme, the platform is still hosted on Google App Engine and is available at https://ds-part-2.ew.r.appspot.com. This application allows users to add/remove tickets to their cart, book all tickets in their cart, and view their previous bookings. If any of the ticket bookings fail (e.g. if one of the tickets has been booked by another user while sitting in the cart), then *none* of the tickets will be booked (the entire booking will fail). Any tickets that have been booked in the process will be released again.

User authentication is done through Firebase, and user data is persisted on Firestore. Bookings are confired via a background Google Pub/Sub worker. Some of the trains on the website (and their corresponding ticket objects) are fetched from third party endpoints, where as others are hosted by our own platform, with associated data managed in the Firestore NoSQL database.

Our report is located in the top level directory of this repo (report-cloud.pdf), which answers a few questions about our implementation. 

I have attacked below a few screenshots of our application.

![image](https://github.com/confinlay/TrainBookingApp/assets/106957733/d9bd3200-2f77-400c-9e69-49e113ec0a7c)
![image](https://github.com/confinlay/TrainBookingApp/assets/106957733/c1c66d88-3756-4ab1-ad80-e59d8ef25445)
![image](https://github.com/confinlay/TrainBookingApp/assets/106957733/ba3d6c40-6518-4c36-a18e-c93f0287c7b1)![image](https://github.com/confinlay/TrainBookingApp/assets/106957733/fe7a787b-2981-4191-b64b-2879c356d016)
