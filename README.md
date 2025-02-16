# NTC - NotTooClose
## Related Publication
> Essaid, Z., Lorenzoni, D., Scatena, N., Xefraj, R., Vecchio, A. (2022). Adaptive Distance Sensing in Contact Tracing Applications Through Indoor/Outdoor Detection. In: Gao, X., Jamalipour, A., Guo, L. (eds) Wireless Mobile Communication and Healthcare. MobiHealth 2021. Lecture Notes of the Institute for Computer Sciences, Social Informatics and Telecommunications Engineering, vol 440. Springer, Cham. https://doi.org/10.1007/978-3-031-06368-8_11
## Description 
<p> The main purpose of this application is to guarantee the 1-meter social distance suggested by OMS to minimize the Covid-19 virus spread in public events.
</p>

## How it works
<p> The application uses the BLE technology (Bluetooth Low Energy) and a Neural Network. </p>
<ol>
  <li> The Neural Network detects if the phone is indoor or outdoor. </li>
  <li> The phone starts receiving RSSI. </li>
  <li> If the RSSI is above a threshold (different from indoor and outdoor) for some device, a notification is generated.</li>
</ol>

![picture](https://github.com/RiccardoXe/NTC/blob/master/doc/NTC.png)
