export type CrossResult = 'ALIGNED_BULLISH' | 'ALIGNED_BEARISH' | 'CONFLICT' | 'UNCERTAIN';
export type Stability = 'stable' | 'unstable' | 'unknown';
export type Confidence = 'High' | 'Medium' | 'Low';

export interface RunSummary {
  runId: string;
  ticker: string;
  executedAt: string;
  stability: Stability | '';
  crossResult: CrossResult | '';
  price: number;
  rsi: number;
  price1dAfter?: number;
  priceChangePct?: number;
}

export interface IpTopAttention {
  ticker: string;
  status: CrossResult;
  oneLine: string;
}

export interface IpDetail {
  ticker: string;
  crossResult: CrossResult;
  summaryText: string;
  technicalSummary: string;
  newsSummary: string;
  sourceUrls: string[];
}

export interface IpOutput {
  runId: string;
  presentedAt: string;
  topAttention: IpTopAttention[];
  marketOverview: string;
  sectorHighlights: { sector: string; dominantResult: string; description: string }[];
  details: IpDetail[];
  reasoning: string;
  closingStatement: string;
}

export interface TrConflict {
  type: string;
  description: string;
}

export interface TrOutput {
  runId: string;
  ticker: string;
  stability: Stability;
  confidence: Confidence;
  conflicts: TrConflict[];
  warnings: TrConflict[];
  reasoning: string;
}

export interface CaOutput {
  runId: string;
  ticker: string;
  crossResult: CrossResult;
  newsDirection: string;
  technicalDirection: string;
  confidence: Confidence;
  reasoning: string;
}

export interface FullResult {
  runId: string;
  ticker: string;
  executedAt: string;
  tr: TrOutput;
  ca: CaOutput;
  ip: IpOutput;
}

export interface RunDetail extends RunSummary {
  fullResult?: FullResult;
  price1wAfter?: number;
}

export interface UserCommitRequest {
  runId: string;
  ticker: string;
  userCrossResult: CrossResult;
  userNote: string;
  agreedWithAi: boolean;
}
