const express = require("express");
const bodyParser = require("body-parser");
const admin = require("firebase-admin");
const path = require("path");

const serviceAccount = require("./serviceAccountKey.json");

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const app = express();
const PORT = 3000;

app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());
app.use(express.static("public"));

// Enviar notificación a un token específico
app.post("/send", async (req, res) => {
  const { token, title, body } = req.body;

  const message = {
    notification: {
      title: title,
      body: body,
    },
    token: token,
  };

  try {
    const response = await admin.messaging().send(message);
    res.send(`Mensaje enviado con éxito: ${response}`);
  } catch (error) {
    console.error("Error al enviar el mensaje:", error);
    res.status(500).send("Error al enviar el mensaje");
  }
});

app.listen(PORT, "0.0.0.0", () => {
  console.log(`Servidor FCM corriendo en http://192.168.100.8:${PORT}`);
});
