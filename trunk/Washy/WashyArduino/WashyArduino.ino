#include <SPI.h>
#include <Ethernet.h>

byte mac[] = {0x90, 0xA2, 0xDA, 0x00, 0xEC, 0xF2};

IPAddress ip(192,168,2,222);
IPAddress server(192,168,2,122);

EthernetClient client;

long lastConnectionTime = 0;
boolean lastConnected = false;
const int postingInterval = 1000;
const int nd = 4;
const int ns = 100;
int data[nd];

void setup() {
  Serial.begin(19200);
  delay(1000);
  Ethernet.begin(mac, ip);
  
  
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
  int ns = 100;
  int nd = 4;
  int laundryData[nd][ns];
  Serial.println("win");
  for(int i = 0; i < ns; i++) {
    for(int j = 0; j < nd; j++) {
      laundryData[j][i] = analogRead(j);
    }
  }
  Serial.println("win");
  for(int j = 0; j < nd; j++) {
    int total = 0;
    for(int i = 0; i < ns; i++) {
      total = total + laundryData[j][i];
    }
    data[j] = total / 100;
  }
}



void sendData(int data[]) {
  if (client.connect(server, 8080)) {
    Serial.println("connected");
    client.print("GET /Washy/servlet/upload");
    for(int j = 0; j < nd; j++) {
      client.print(",");
      client.print(data[j]);
    }
    client.println("   HTTP/1.0");
    client.println();
    client.stop();
    Serial.println("sent");    
    lastConnectionTime = millis();
  } else {
    Serial.println("connection failed sleeping for 1 second");
    delay(1000);
  }
}
