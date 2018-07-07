# dempster-shafer-location-estimation

Sample Java code to perform location estimation using Dempster-Shafer Theory. 

It is used to estimate the location for an event which is represented by a collection of tweets. It assumes that tweets and cities (or any type of location) are stored on a database. Cities are represented by a name and boundary coordinates, and tweets can have latitude longitude, text (tweet content) and user location. Event localization is performed based on available spatial features in tweets about that event.

Details of the method can be found in our paper:
[O. Ozdikis, H. Oguztüzün, P. Karagoz. Evidential Estimation of Event Locations in Microblogs Using the Dempster-Shafer Theory, Inf. Process. Manage., vol. 52(6), pp.1227-1246, 2016](https://dl.acm.org/citation.cfm?id=2533929)
