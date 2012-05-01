const int nd = 4;     // number of channels
const int ns = 10000; // number of samples
const int nol = 10;   // number of outliers (10 outliers for 10000 = TP99.9)
int data[nd];

void setup() {
    Serial.begin(19200);
}

void loop() {
  gatherData(data);
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
    Serial.print(topNol[j][nol - 1]);
    Serial.print(" ");
  }
  Serial.println();
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


