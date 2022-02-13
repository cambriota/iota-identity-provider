import * as dotenv from "dotenv";
import express from "express";
import cors from "cors";
import helmet from "helmet";
import { check } from "./identity";
import { Presentation } from "@iota/identity-wasm/node";

// env
dotenv.config();

if (!process.env.PORT) {
  process.exit(1);
}

const PORT: number = parseInt(process.env.PORT as string, 10);

const app = express();

// app
app.use(helmet());
app.use(cors());
app.use(express.urlencoded());
app.use(express.json());

// server
app.listen(PORT, () => {
  console.log(`Listening on port ${PORT} ...`);
});

// endpoints
app.post("/verify", async (req, res) => {
  const result = await check(req.body);
  if (result.error) {
    res.status(400);
  }
  res.send(result);
});

app.get("/health", (req, res) => {
  res.send({ status: "UP" });
});
