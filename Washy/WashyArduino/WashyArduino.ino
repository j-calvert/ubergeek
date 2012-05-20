#include <Dhcp.h>
#include <util.h>
#include <EthernetServer.h>
#include <Dns.h>
#include <Ethernet.h>
#include <EthernetUdp.h>
#include <EthernetClient.h>





#include <SPI.h>
#include <Ethernet.h>


//IPAddress ip(5,1,178,8);
IPAddress ip(192,168,2,211); // works when wired to WRTG54gG
//IPAddress ip(10,0,0,142);
//IPAddress ip(101,178,8,5);
byte mac[] = {0x90, 0xA2, 0xDA, 0x00, 0xEC, 0xF2};
// char serverName[] = "www.google.com"; // works (with DNS)
char serverName[] = "washy.wilana.org"; // works running on laptop (as of now)

EthernetClient client;

const int PORT = 80;
long lastConnectionTime = 0;
boolean lastConnected = false;
const int postingInterval = 100;
const int nd = 4;     // number of chanenels
const int ns = 10000; // number of samples
const int nol = 10;   // number of outliers (10 outliers for 10000 = TP99.9)
int data[nd];

void setup() {
  Serial.begin(19200);
  delay(5000);
  Ethernet.begin(mac, ip);
    // no point in carrying on, so do nothing forevermore:
//    while(Ethernet.begin(mac) == 0) {
//      delay(1000);
//      Serial.println("Failed to configure Ethernet using DHCP");
//  }
  
  
  delay(1000);
}

void loop() {
  Serial.println("gathering data");
  gatherData(data);
  if (!client.connected() && lastConnected) {
    client.stop();
  }
  if(millis() - lastConnectionTime < postingInterval) {
    Serial.println("sleeping a bit");
    delay(postingInterval - (millis() - lastConnectionTime));
    
  }
  
  Serial.println("sending data");
  if(!client.connected()) {
    sendData(data);
  }
  lastConnected = client.connected();  
}

void gatherData(int data[]) {
  int topNol[nd][nol];
  for(int j = 0; j < nd; j++) {
    for(int i = 0; i < nol; i++) {
      topNol[j][i] = 0;
    }
  }
  for(int i = 0; i < ns; i++) {
    for(int j = 0; j < nd; j++) {
      updateTopNol(topNol[j], analogRead(j));
    }
  }
  for(int j = 0; j < nd; j++) {
    data[j] = topNol[j][nd - 1];
    for(int i = 0; i < nol; i++) {
      Serial.print(topNol[j][i]);
      Serial.print(" ");
    }
    Serial.println();
  }
}

void updateTopNol(int topNol[], int s) {
   boolean inserted = false;
   for(int i = 0; i < nol; i++) {
     if(!inserted && topNol[i] < s) {
        insert(topNol, i, s);
        inserted = true;
     }
   }
}

void insert(int topNol[], int i, int s) {
  int tmp = s;
  for(int j = nd-1; j > i; j--) {
    topNol[j] =  topNol[j - 1];
  }
  topNol[i] = s;
}

void sendData(int data[]) {
  if (client.connect(serverName, PORT)) {
    Serial.println("connected");
    client.print("GET /upload/");
    for(int j = 0; j < nd; j++) {
      if(j > 0) {
        client.print(",");
      }
      client.print(data[j]);
    }
    client.println(" HTTP/1.0");
    client.println();
    client.stop();
    Serial.println("sent");    
    lastConnectionTime = millis();
  } else {
    Serial.println("connection failed sleeping for 1 second");
    delay(1000);
  }
}
